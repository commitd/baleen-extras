/**
 * Patterns are blocks of text between two entities (or other significant annotator).
 *
 * They are used in the context of relation extraction to help learn the interaction words used to
 * determine if a relationship may be present in the sentence.
 *
 * The process to converting patterns to interaction words as follow:
 *
 * <ol>
 * <li>Extract patterns into a database
 * <li>Learn interaction words based on the saved patterns
 * <li>Generalise interaction words to cover similar concepts
 * <li>Save the interaction words together with other information into a database for use in
 * pipelines.
 * </ol>
 *
 * As an alternative to the three two steps the user / corpus owner might already have an idea how
 * of the interaction terms that they wish to extract. For example in a genelogy application the
 * user might only want to extract interaction words which establish a family connection, for
 * example "brother", "sister", "uncle of". As such the user may wish to manually craft the
 * interaction gazetteer and upload it via the last step.
 *
 *
 * The interaction words can then bed used in a pipeline, @see
 * com.tenode.baleen.extras.interactions.annotators for details.
 *
 * <h3>Pattern extraction</h3>
 *
 * <ul>
 * <li>Determine where entities are in the document, using a standard entity extraction pipeline.
 * <li>Extract all the patterns between the entities
 * <li>Save the patterns to the database.
 * </ul>
 *
 * You must ensure the output of the entity extraction is as good as possible. Any entities not
 * found are candidates for pattern / interaction words!
 *
 * The PatternExtractor annotator in Baleen is based on the principles in Ubmre paper (@see
 * {com.tenode.ata.relationships}). Ultimately is has a simple operation. It extracts the words
 * between the two entities per sentence. These are the patterns. The patterns are filtered such
 * that:
 * <ul>
 * <li>Patterns are not negative (eg that is do not contain "no, not, neither"). Thus
 * "John does not live in London" is not a pattern.
 * <li>Any other entities are removed. Thus Jane is removed when considering the pattern betten John
 * and London in the sentence "John and Jane live in London". The pattern become "and live in".
 * <li>The words in the pattern are not stop words. Thus from the "and lives in" we have a pattern
 * based of "lives". If we had the sentence "John frequently visits parts of London" then our
 * pattern, without stop words, would be "frequently visits parts" (though the stop words list may
 * be configured differently).
 *
 * The patterns are saves as the Pattern type.
 *
 * Note that language features (Sentence spliting, tokenisation, POS) need to have been performed
 * before using the Pattern extractor. For example use the OpenNlp annotator - this is likely part
 * of the pipeline anyhow.
 *
 * A MongoPatternSaver consumer is available in Baleen which will output the patterns into a Mongo
 * database. As usual the Mongo database is provided as a shared resource and the output collection
 * can be tailored.
 *
 * Thus the pipeline is:
 *
 * <pre>
 *
 * annotators:
 * - # Standard Baleen entity pipeline
 * - com.tenode.baleen.extras.patterns.annotators.PatternExtractor
 *
 * consumers:
 * - com.tenode.baleen.extras.patterns.consumers.MongoPatternSaver
 * </pre>
 *
 *
 * <h3>Identifying interaction words</h3>
 *
 * The process by which interaction words are identified is run as a Baleen job. It does not form
 * part of a pipeline because it requires the output of multiple documents to have been passed
 * through the Pattern extraction pipeline as discussed above.
 *
 * This is based on the algorithm within [UBRME]. Other implementation are possible, including for
 * example merely saving all verbs / nouns which are need more than a specific threshold in the
 * patterns.
 *
 * We do not go into details here of the algorithm, but it effectively looks to find clusters of the
 * pattern extracted. it then looks within those clusters for common words, which is saves a
 * interaction words.
 *
 * <pre>
 * mongo:
 *  db: baleen
 *  host: localhost
 *
 * job:
 *  tasks:
 *  - class: com.tenode.baleen.extras.patterns.jobs.IdentifyInteractions
 *    filename: output/interactions.csv
 *
 * </pre>
 *
 * The interactions.csv will be written. It should be reviewed by a subject matter expert and rows
 * removed where relations are invalid or added (with different type constraints).
 *
 * <h3>Enhancing interaction</h3>
 *
 * The interaction words output from the above are those seen in the document. Optionally, we can
 * enhance and generalise these with another job.
 *
 * The job will read the interaction.csv and add alternative works. For example the verb "report"
 * might have alternatives "communicate, broadcast".
 *
 * <pre>
 *
 * job:
 * tasks:
 * - class: com.tenode.baleen.extras.patterns.jobs.EnhanceInteractions
 *   input: output/interactions.csv
 *   output: output/interactions-enhanced.csv
 * </pre>
 *
 * Again this is an opportunity to review the CSV file in order to ensure that the alternatives are
 * correct.
 *
 * <h3>Upload interactions</h3>
 *
 * Finally we output the interaction (manually created, basic, or enhanced) to the Mongo. This reads
 * the CSV files and saves the data to Mongo. It is saves into two collections, the first
 * interactions (which is Baleen Mongo gazetteer format) and the second relationTypes (which
 * includes information about interaction work types which are used by relation constraints).
 *
 *
 * <pre>
 * mongo:
 *  db: baleen
 *  host: localhost
 *
 * job:
 *  tasks:
 *  - class: com.tenode.baleen.extras.patterns.jobs.UploadInteractionsToMongo
 *    input: output/interactions-enhanced.csv
 *
 * </pre>
 */
package com.tenode.baleen.extras.patterns;