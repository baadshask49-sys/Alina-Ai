package com.alina.ai;

/**
 * Reference table for the Phase 5 Hindi/Hinglish viseme system.
 *
 * A  = open vowel (a/aa)
 * E  = spread vowel (e/i)
 * O  = rounded vowel (o/u)
 * MBP = closed lips (m/b/p)
 * TDN = tongue-tip consonants (t/d/n)
 * SSH = s/sh family
 * R  = r-family
 *
 * Runtime scheduling is performed by HindiVisemeEngine in MainActivity.
 * For true phoneme timestamps, replace the estimator with a TTS engine/API
 * that returns word/phoneme timing.
 */
public final class HindiVisemeMap {
    private HindiVisemeMap() {}
}
