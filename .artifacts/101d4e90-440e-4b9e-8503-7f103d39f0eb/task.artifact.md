# Task List - Autofill Manual Backup Refinement

- [x] **Phase 1: Autofill Service Core Updates**
    - [x] Add `withTimeoutOrNull` to prevent hanging on DataStore/Repo calls
    - [x] Implement manual backup suggestions for the active focused field
    - [x] Refine `Dataset` creation to support single-field targeted filling
- [x] **Phase 2: Suggestion Messaging & UI**
    - [x] Update `createGenerationDataset` to fill both fields simultaneously
    - [x] Update labels for manual suggestions
- [x] **Phase 3: Verification**
    - [x] Test long-press -> Autofill menu in Google Tasks/Keep
    - [x] Verify that manual fill works even when automatic detection fails
    - [x] Confirm both matched credentials and generation are available as fallbacks
