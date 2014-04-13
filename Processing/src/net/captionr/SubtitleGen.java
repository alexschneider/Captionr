package net.captionr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechAligner;
import edu.cmu.sphinx.result.WordResult;

public class SubtitleGen {
	private static final long MILLISECONDS_PER_HOUR = 3600000;
	private static final long MILLISECONDS_PER_MINUTE = 60000;

	public static void generateSubtitle(File audio, Path transcriptIn, File subtitle) throws IOException {
		String transcript = readFile(transcriptIn);
		String processedTranscript = strip(transcript);
		
		Configuration configuration = new Configuration();
		 
		configuration.setAcousticModelPath("file:/home/andrewakers/gitrepos/cmusphinx-code/sphinx4/models/en-us");  // TODO: Generalize
		configuration.setDictionaryPath("file:/home/andrewakers/gitrepos/cmusphinx-code/cmudict/sphinxdict/cmudict.0.7a_SPHINX_40");
		
		SpeechAligner aligner = new SpeechAligner(configuration);
		
		URL audioURL = audio.toURI().toURL();
		List<WordResult> output = aligner.align(audioURL, processedTranscript);
		
		audio.delete();
		transcriptIn.toFile().delete();
		
		writeVTT(output, transcript, subtitle);
	}

	private static void writeVTT(List<WordResult> output, String transcript, File subtitle) throws IOException {
		System.out.println(subtitle.getAbsolutePath());
		if (!subtitle.exists()) {
			subtitle.createNewFile();
		}
		
		FileWriter fw = new FileWriter(subtitle);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("WEBVTT\n\n");
		
		LinkedList<String> lines = new LinkedList<String>(Arrays.asList(transcript.split("\n")));
		
		StringBuilder s1, s2 = null;
		
		Iterator<WordResult> itr = output.iterator();
		
		while (lines.peek() != null) {
			WordResult w = itr.next();
			s1 = new StringBuilder();
			s1.append(timeString(w.getTimeFrame().getStart()) + " --> ");
			
			s2 = new StringBuilder();
			String[] words = lines.pop().split(" ");
			
			for (int i = 0; i < words.length; i++) {
				String[] hyphen = words[i].split("-");
				for (int i1 = 0; i1 < hyphen.length; i1++) {
					while (getString(w).equals("<sil>")) {
						w = ifHasNext(itr);
					}
					System.out.println("Have: " + hyphen[i1] + " Current time index: " + w.toString());
					
					System.out.println("Trying to match " + strip(hyphen[i1]) + " with " + getString(w));
					if (strip(hyphen[i1]).equals(getString(w))) {
						w = ifHasNext(itr);
					}
				}
				if (s2.length() + words[i].length() + 1 > 30) {
					if (s2.length() + words[i].length() + 1 > 40) {
						lines.push(makeString(Arrays.copyOfRange(words, i, words.length)));
						i = words.length; // break out of the for loop
					} else {
						s2.append(words[i] + " ");
						if (getString(w).equals("<sil>")) {
							lines.push(makeString(Arrays.copyOfRange(words, i + 1, words.length)));
							i = words.length; // break out of the for loop
						}
					}
				} else {
					s2.append(words[i] + " ");
				}
			}
			
			s1.append(timeString(w.getTimeFrame().getEnd()));
			bw.write(s1.toString() + "\n" + s2.toString() + "\n\n");
		}
		
		bw.close();
	}
	
	private static String makeString(String[] strings) {
		StringBuilder sb = new StringBuilder();
		for (String s : strings) {
			sb.append(s + " ");
		}
		return sb.toString();
	}

	private static WordResult ifHasNext(Iterator<WordResult> itr) {
		if (itr.hasNext()) {
			return itr.next();
		} else {
			return null;
		}
	}

	private static String getString (WordResult w) {
		if (w == null) {
			return "";
		}
		return w.toString().replaceAll("[^a-z<>\']", "");
	}

	private static String timeString(long millis) {
		long hours = millis / MILLISECONDS_PER_HOUR;
		millis %= MILLISECONDS_PER_HOUR;
		long minutes = millis / MILLISECONDS_PER_MINUTE;
		millis %= MILLISECONDS_PER_MINUTE;
		long seconds = millis / 1000;
		millis %= 1000;
		return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis);
	}

	private static String strip(String s) {
		return s.replaceAll("[\n-]", " ").replaceAll("[^A-Za-z\' ]", "").toLowerCase();
	}

	private static String readFile (Path txt) throws IOException {
		byte[] encoded = Files.readAllBytes(txt);
		return new String(encoded, StandardCharsets.UTF_8);
	}
}
