package de.l3s.eventkg.yago;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.l3s.eventkg.integration.AllEventPagesDataSet;
import de.l3s.eventkg.meta.Language;
import de.l3s.eventkg.meta.Source;
import de.l3s.eventkg.pipeline.Extractor;
import de.l3s.eventkg.util.FileLoader;
import de.l3s.eventkg.util.FileName;
import de.l3s.eventkg.yago.model.YAGOMetaFact;

/**
 * Collects all triples with an event as subject or object. Also loads all facts
 * with temporal meta facts. Results are written to two files.
 */
public class YAGOEventRelationsExtractor extends Extractor {

	private AllEventPagesDataSet allEventPagesDataSet;

	public YAGOEventRelationsExtractor(List<Language> languages, AllEventPagesDataSet allEventPagesDataSet) {
		super("YAGOEventRelationsExtractor", Source.YAGO, "?", languages);
		this.allEventPagesDataSet = allEventPagesDataSet;
	}

	public void run() {
		System.out.println("Collect event pages.");
		extractTriples();
	}

	private void extractTriples() {

		System.out.println("Collect meta facts.");

		Map<String, Set<YAGOMetaFact>> temporalMetaFacts = YAGOMetaFactsDataSet.loadMetaFacts(true);

		// Properties:
		// <isLeaderOf>, <owns>, <isLocatedIn>, <isCitizenOf>, <hasMusicalRole>
		// <hasOfficialLanguage>, <edited>, <isConnectedTo>, <actedIn>,
		// <imports>, <wasBornIn>, <participatedIn>, <dealsWith>, <diedIn>,
		// <created>, <isPoliticianOf>, <wroteMusicFor>, <hasNeighbor>,
		// <isMarriedTo>, <hasChild>, <hasGloss>, <isInterestedIn>,
		// <isAffiliatedTo>, <hasCurrency>, <exports>, <happenedIn>,
		// <hasGender>, <playsFor>, <directed>, <worksAt>, <graduatedFrom>,
		// <hasCapital>, <influences>, <hasWonPrize>, <hasWebsite>, <livesIn>,
		// <hasAcademicAdvisor>, <isKnownFor>

		System.out.println("Start writing to files.");
		PrintWriter eventFactsWriter = null;
		PrintWriter temporalFactsWriter = null;
		try {
			eventFactsWriter = FileLoader.getWriter(FileName.YAGO_EVENT_FACTS);
			temporalFactsWriter = FileLoader.getWriter(FileName.YAGO_TEMPORAL_FACTS);

			BufferedReader br = null;
			try {
				try {
					br = FileLoader.getReader(FileName.YAGO_FACTS);
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}

				String line;
				String previousId = null;

				while ((line = br.readLine()) != null) {

					if (line.startsWith("#@ <id")) {
						previousId = line.substring(3);
					}

					if (line.isEmpty() || line.startsWith("@") || line.startsWith("#"))
						continue;

					String[] parts = line.split("\t");

					String wikipediaLabel1 = parts[0].substring(1, parts[0].length() - 1);
					String wikipediaLabel2 = parts[2].substring(1, parts[2].length() - 3);
					String property = parts[1];

					Set<YAGOMetaFact> metaFactsOfFact = temporalMetaFacts.get(previousId);

					// a) Relations to events
					if (allEventPagesDataSet.getEventByWikipediaLabel(Language.EN, wikipediaLabel1) != null
							|| allEventPagesDataSet.getEventByWikipediaLabel(Language.EN, wikipediaLabel2) != null) {
						eventFactsWriter.write(wikipediaLabel1 + "\t" + property + "\t" + wikipediaLabel2 + "\n");
						if (metaFactsOfFact != null) {
							for (YAGOMetaFact metaFact : metaFactsOfFact) {
								eventFactsWriter
										.write("\t" + metaFact.getProperty() + "\t" + metaFact.getObject() + "\n");
							}
						}
						continue;
					}

					// b) Facts with temporal scope
					if (metaFactsOfFact != null) {
						temporalFactsWriter.write(wikipediaLabel1 + "\t" + property + "\t" + wikipediaLabel2 + "\n");
						for (YAGOMetaFact metaFact : metaFactsOfFact) {
							temporalFactsWriter
									.write("\t" + metaFact.getProperty() + "\t" + metaFact.getObject() + "\n");
						}
					}

				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} finally {
			eventFactsWriter.close();
			temporalFactsWriter.close();
		}

	}

}
