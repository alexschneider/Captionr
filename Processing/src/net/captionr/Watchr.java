package net.captionr;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.*;

public class Watchr {
	private static final Path AUDIO_WATCH_PATH = Paths.get("/opt/captionr/data/audio/");
	private static final String TXT_WATCH_PATH = "/opt/captionr/data/transcript/";
	private static final String VTT_DROP_PATH = "/opt/captionr/data/subtitle/";
	
    private WatchService watcher;
    private Map<WatchKey,Path> keys;
    private boolean trace = false;
    
    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }
    
    /**
     * Register the given directory with the WatchService
     */
    private void register() throws IOException {
        WatchKey key = AUDIO_WATCH_PATH.register(watcher, ENTRY_CREATE);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                System.out.format("register: %s\n", AUDIO_WATCH_PATH);
            } else {
                if (!AUDIO_WATCH_PATH.equals(prev)) {
                    System.out.format("update: %s -> %s\n", prev, AUDIO_WATCH_PATH);
                }
            }
        }
        keys.put(key, AUDIO_WATCH_PATH);
    }

    /**
     * Creates a WatchService and registers the audio directory
     */
    Watchr() throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey,Path>();

        register();

        // enable trace after initial registration
        this.trace = true;
    }

    /**
     * Process all events for keys queued to the watcher
     */
    @SuppressWarnings("rawtypes")
	void processEvents() {
        for (;;) {

            // wait for key to be signaled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                // TBD - provide example of how OVERFLOW event is handled
                if (kind == OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                File child = dir.resolve(name).toFile();
                
                String ID = child.getName().split("\\.")[0];
                URL audio = null;
                
                try {
					audio = child.toURI().toURL();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                
                Path transcript = Paths.get(TXT_WATCH_PATH, ID + ".txt");
                File subtitle = Paths.get(VTT_DROP_PATH, ID + ".vtt").toFile();
                
                try {
					SubtitleGen.generateSubtitle(audio, transcript, subtitle);
				} catch (IOException e) {
					e.printStackTrace();
				}
                
                // print out event
                System.out.format("%s: %s\n", kind.name(), child);
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }

    static void usage() {
        System.err.println("usage: java Watchr [-r]");
        System.exit(-1);
    }

    public static void main(String[] args) throws IOException {
        new Watchr().processEvents();
    }
}
