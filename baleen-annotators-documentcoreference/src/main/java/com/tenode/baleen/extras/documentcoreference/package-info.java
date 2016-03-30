/**
 * Implementation of document coreference (finding mentions of the same entity within a document).
 *
 * The implementation is based on the Stanford CoreNLP paper (see
 * {@link com.tenode.baleen.extras.documentcoreference.annotators.Coreference} for more information.
 *
 * Use of this annotator is very simple. Assuming you have a complete Baleen entity extraction
 * pipeline the only required addition is Coreference:
 *
 * <pre>
 * annotators:
 * - # entity extraction
 * - # entity cleaners
 * # New addition for coreference, requesting pronomial resolution in addition to entity resolution.
 * - class: com.tenode.baleen.annotators.documentcoreference.Coreference
 *   pronomial: true
 * </pre>
 *
 * In order to run this you should have previously created WorkToken, PhraseChunks (thgouh an
 * constituent parser or chunker) and extracted all entity (Entity subtypes) of interest. If you
 * have previously coreferenced to items by other means that will be respected.
 *
 * The pronomial settings is the only important configuration detail. Resolving pronouns (eg "he" to
 * "John SMith") is not necessarily useful in some cases and is lower confidence.
 *
 * Following this annotator ReferentTarget will be create and set Entity, WordTokens and
 * PhraseChunks.
 *
 * Many Baleen components to not work with non-Entity ReferentTypes. In those cases the
 * {@link com.tenode.baleen.extras.documentcoreference.annotators.ReferentToEntity} annotator will
 * convert items like pronoun WordTokens to a full entity (if they are linked to an entity through
 * coreference). This can be useful for output or onwards processing (which was designed
 * specifically for entities and which has not been made ReferentTarget aware).
 *
 * NOTE you can write the output of coreference to CSV using @see
 * {@link com.tenode.baleen.extras.common.consumers.CsvCoreferenceConsumer}.
 *
 */
package com.tenode.baleen.extras.documentcoreference;
