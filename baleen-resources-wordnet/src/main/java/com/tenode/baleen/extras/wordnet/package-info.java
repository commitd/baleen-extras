/**
 * Baleen component and commandline utilities to work with WordNet.
 *
 * Within Baleen the standard use case will be simple to use a shared resource of type
 * {@link com.tenode.baleen.extras.wordnet.resources.WordNetResource}. It is unlikely that most users will
 * need to declare this in their YAML and may simply use it via an @ExternalResource.
 *
 * An annotator is included to perform add the Lemma form to WordToken's if it has been omitted.
 *
 * This package contains a helper utilities for get POS instances and command line application to
 * get SuperSense of words.
 */
package com.tenode.baleen.extras.wordnet;
