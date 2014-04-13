package net.captionr;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechAligner;
import edu.cmu.sphinx.result.WordResult;

public class Testr {

	public static void main (String[] args) throws IOException {
		URL audio = new URL("file:///home/andrewakers/gitrepos/LA-Hacks-Captioning/Processing/test-files/ted.wav");
		String transcript = readFile("/home/andrewakers/gitrepos/LA-Hacks-Captioning/Processing/test-files/ted.txt");
		String align = strip(transcript);
		System.out.println(align);
		
		Configuration configuration = new Configuration();
		 
		configuration.setAcousticModelPath("file:/home/andrewakers/gitrepos/cmusphinx-code/sphinx4/models/en-us");
		configuration.setDictionaryPath("file:/home/andrewakers/gitrepos/cmusphinx-code/cmudict/sphinxdict/cmudict.0.7a_SPHINX_40");
		
		SpeechAligner aligner = new SpeechAligner(configuration);
//		aligner.startRecognition(audio.openStream());
		List<WordResult> output = aligner.align(audio, align);
		
//		System.out.println(aligner.getResult());
		for (WordResult o : output) {
			System.out.println(o.toString());
		}
	}
	
	
	private static String strip(String transcript) {
		return transcript.replaceAll("[\n-]", " ").replaceAll("[^A-Za-z\' ]", "").toLowerCase();
	}


	private static String readFile (String path) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, StandardCharsets.UTF_8);
	}
}
