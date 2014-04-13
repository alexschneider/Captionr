package net.captionr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechAligner;
import edu.cmu.sphinx.result.WordResult;

public class SubtitleGen {
	private static final long MILLISECONDS_PER_HOUR = 3600000;
	private static final long MILLISECONDS_PER_MINUTE = 60000;

	public static void generateSubtitle(URL audio, Path transcriptIn, File subtitle) throws IOException {
		String transcript = readFile(transcriptIn);
		String processedTranscript = strip(transcript);
		
		Configuration configuration = new Configuration();
		 
		configuration.setAcousticModelPath("file:/home/andrewakers/gitrepos/cmusphinx-code/sphinx4/models/en-us");  // TODO: Generalize
		configuration.setDictionaryPath("file:/home/andrewakers/gitrepos/cmusphinx-code/cmudict/sphinxdict/cmudict.0.7a_SPHINX_40");
		
		SpeechAligner aligner = new SpeechAligner(configuration);
		List<WordResult> output = aligner.align(audio, processedTranscript);
		
		writeVTT(output, transcriptIn, subtitle);
	}

	private static void writeVTT(List<WordResult> output, Path transcript, File subtitle) throws IOException {
		System.out.println(subtitle.getAbsolutePath());
		if (!subtitle.exists()) {
			subtitle.createNewFile();
		}
		
		FileWriter fw = new FileWriter(subtitle);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("WEBVTT\n\n");
		
		FileReader fr = new FileReader(transcript.toFile());
		Scanner sr = new Scanner(fr);
		
		StringBuilder s1, s2 = null;
		Iterator<WordResult> itr = output.iterator();
		
		while (sr.hasNextLine()) {
			WordResult w = itr.next();
			s1 = new StringBuilder();
			s1.append(timeString(w.getTimeFrame().getStart()) + " --> ");
			
			s2 = new StringBuilder();
			String[] words = sr.nextLine().split(" ");
			
			for (int i = 0; i < words.length; i++) {
				if (strip(words[i]).equals(getString(w))) {
					w = itr.next();
				}
				s2.append(words[i] + " ");
			}
			
			s1.append(timeString(w.getTimeFrame().getStart()));
			bw.write(s1.toString() + "\n" + s2.toString() + "\n\n");
		}
		
		sr.close();
		bw.close();
	}
	
	private static String getString (WordResult w) {
		return w.toString().replaceAll("[^a-z]", "");
	}

	private static String timeString(long millis) {
		long hours = millis / MILLISECONDS_PER_HOUR;
		millis %= MILLISECONDS_PER_HOUR;
		long minutes = millis / MILLISECONDS_PER_MINUTE;
		millis %= MILLISECONDS_PER_MINUTE;
		long seconds = millis / 1000;
		millis %= 1000;
		return String.format("%2d:%2d:%2d.%3d", hours, minutes, seconds, millis);
	}

	private static String strip(String s) {
		return s.replaceAll("[\n-]", " ").replaceAll("[^A-Za-z\' ]", "").toLowerCase();
	}

	private static String readFile (Path txt) throws IOException {
		byte[] encoded = Files.readAllBytes(txt);
		return new String(encoded, StandardCharsets.UTF_8);
	}
}
