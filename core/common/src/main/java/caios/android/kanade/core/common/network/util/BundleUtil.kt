import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle

fun buildBundle(f: Bundle.() -> Unit): Bundle {
    return Bundle().apply(f)
}

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
