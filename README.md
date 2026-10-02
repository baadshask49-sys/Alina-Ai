# Alina AI — Phase 5 Hindi Phoneme/Viseme Lip-Sync

Boss-focused upgrade for the Alina character.

## What is included
- Hindi/Hinglish text-to-viseme mapping
- A/Aa, E/I, O/U, M/B/P, T/D/N, S/Sh and R mouth groups
- Timing scheduler that advances mouth shapes while Alina's Hindi TTS is speaking
- Existing Idle, Listening and Thinking states
- Existing neon UI and Boss addressing

## Important technical note
Android's standard TextToSpeech API does not expose phoneme timestamps for every spoken phoneme.
This phase therefore uses a Hindi/Hinglish grapheme-to-viseme estimator synchronized to estimated
speech duration. It is a stronger approximation than random mouth switching.

For true phoneme-level synchronization, the next upgrade should use a speech engine/API that
returns phoneme or viseme timing events. The mouth assets can then be replaced without changing
the UI architecture.
