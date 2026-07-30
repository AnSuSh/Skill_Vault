# Implementation Plan - Autofill Manual Backup Feature

This plan implements a "Manual Backup" solution that allows users to fill data into any focused field, even if automatic field detection fails. This is triggered when the user long-presses a field and selects "Autofill" or as a fallback when normal detection is uncertain.

## Proposed Changes

### 1. Universal Backup Suggestions

#### [MODIFY] [SkillVaultAutofillService.kt](file:///home/ansush/AndroidStudioProjects/SkillVault/app/src/main/java/com/quickthought/skillvault/autofill/SkillVaultAutofillService.kt)
- **Focused-Only Filling**: Update the logic to ensure that if a field is focused, we provide suggestions that target **only** that specific `AutofillId`, in addition to the form-wide suggestions.
- **Always-On Backup**:
    - If a matching credential exists for the site/app, always include "Fill matched [Username/Password]" as individual suggestions targeting the `focusedId`.
    - These will appear even if the service isn't 100% sure if the focused field is for a username or password.
- **Manual Request Flag**: If `request.flags` contains `FLAG_MANUAL_REQUEST`, include a broader set of data (e.g., common emails, most used credentials) to fill the focused field.
- **Dataset Labels**: Use clear labels for backup actions, such as:
    - "Manual: Fill matched Email"
    - "Manual: Fill matched Password"
    - "Manual: Generate Username"
    - "Manual: Generate Password"

### 2. Improved Robustness

#### [MODIFY] [SkillVaultAutofillService.kt](file:///home/ansush/AndroidStudioProjects/SkillVault/app/src/main/java/com/quickthought/skillvault/autofill/SkillVaultAutofillService.kt)
- **Relaxed Matching**: For manual requests, reduce the strictness of field-type matching. If the user asks for help, show all logical options for that field.
- **Persistence**: Ensure suggestions persist if the user switches between fields within the same session.

## Verification Plan

### Manual Verification
- **Test with Unidentified Fields**:
    - Find a text input that Skill Vault doesn't automatically recognize.
    - Focus it -> Tap the suggestion area.
    - Verify that "Manual: Fill matched Email" etc. appear.
    - Tap one -> Verify it fills the focused field.
- **Long-Press Menu**:
    - Long-press a field -> Select "Autofill" (system menu).
    - Verify that Skill Vault offers the full list of backup options.
- **Chrome/Firefox**:
    - Verify that even if the browser obscures some form info, the focused field can still be filled using these manual options.
