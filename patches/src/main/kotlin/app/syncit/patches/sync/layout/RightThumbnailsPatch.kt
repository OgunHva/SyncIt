package app.syncit.patches.sync.layout

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.patch
import app.morphe.patcher.util.getReference
import app.syncit.patches.sync.Constants.SYNC_FOR_REDDIT
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

/** Loads the app's settings, the thumbnail alignment included. */
internal val settingsFingerprint = Fingerprint(
    definingClass = "Lcom/laurencedawson/reddit_sync/singleton/SettingsSingleton;",
    strings = listOf("thumbnail_align"),
)

@Suppress("unused")
val rightThumbnailsPatch = patch(
    name = "Put list thumbnails on the right",
    description = "Forces the list, compact and smaller card layouts to draw their thumbnails on " +
            "the right side, as if the Align thumbnail right setting were on."
) {
    compatibleWith(SYNC_FOR_REDDIT)

    execute {
        val method = settingsFingerprint.method
        val instructions = method.implementation!!.instructions.toList()

        val keyIndex = instructions.indexOfFirst { instruction ->
            instruction.getReference<StringReference>()?.string == "thumbnail_align"
        }
        check(keyIndex >= 0) { "Could not find the thumbnail alignment setting." }

        val readIndex = (keyIndex + 1 until instructions.size).firstOrNull { index ->
            val reference = instructions[index].getReference<MethodReference>()
            reference?.definingClass == "Landroid/content/SharedPreferences;" &&
                    reference.name == "getBoolean" &&
                    reference.returnType == "Z"
        } ?: error("Could not find where the thumbnail alignment is read.")

        val resultIndex = readIndex + 1
        check(instructions[resultIndex].opcode == Opcode.MOVE_RESULT) {
            "The thumbnail alignment read is not followed by its result."
        }
        val register = (instructions[resultIndex] as OneRegisterInstruction).registerA

        // Ignore the stored value and always take the right-aligned branch.
        method.replaceInstruction(resultIndex, "const/4 v$register, 0x1")
    }
}
