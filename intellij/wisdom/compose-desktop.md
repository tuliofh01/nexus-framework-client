# Compose Desktop wisdom

This client uses **Compose Multiplatform Desktop** (JetBrains), not Android XML Views.

## Imports

```kotlin
import androidx.compose.material.*          // Material 1 APIs used by this client
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.foundation.*
```

Do not pull `android.app.*` / AndroidX Activity into `:app`.

## Text fields on dark UI

Always set both `textStyle` color and `TextFieldDefaults.outlinedTextFieldColors(textColor = …)`.

Use:

- `label` for the floating caption
- `placeholder` for the empty-state hint (clears when the user types)
- empty `value` when you want the placeholder visible

Avoid stuffing a fake default like `"MyApp"` into `value` — it looks stuck and forces deletion.

## Entry

`compose.desktop { application { mainClass = "com.nexus.framework.AppKt" } }`
