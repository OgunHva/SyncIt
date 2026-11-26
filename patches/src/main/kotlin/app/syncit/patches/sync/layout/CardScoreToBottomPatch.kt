package app.syncit.patches.sync.layout

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.patch
import app.morphe.patcher.patch.resourceXml
import app.morphe.patcher.util.getReference
import app.syncit.patches.sync.Constants.SYNC_FOR_REDDIT
import app.syncit.patches.sync.extension.sharedExtensionPatch
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/syncit/extension/CardScoreReorder;"

/** Draws a post's description, which holds the author line and the score line. */
internal val postDescriptionFingerprint = Fingerprint(
    definingClass =
        "Lcom/laurencedawson/reddit_sync/ui/views/text/spannable/children/posts/PostDescriptionTextView;",
    name = "H",
    returnType = "V",
    parameters = listOf("Lxa/d;", "I", "Z", "Z", "Z"),
)

@Suppress("unused")
val cardScoreToBottomPatch = patch(
    name = "Move the card score to the bottom",
    description = "Splits the score line out of the post description in the smaller card layout, " +
            "so the title stays at the top, the author and flair follow with even gaps, and the " +
            "points and comments sit at the bottom. List, compact and full card layouts are unchanged."
) {
    compatibleWith(SYNC_FOR_REDDIT)

    dependsOn(sharedExtensionPatch)

    execute {
        resourceXml("res/layout/holder_smaller_cards.xml").use { layout ->
            val wrapper =
                layout.elementByAttribute("android:id", "@id/holder_list_text_wrapper")
            val description =
                layout.elementByAttribute("android:id", "@id/holder_list_description")
            val flair =
                layout.elementByAttribute("android:id", "@id/holder_list_generic_extra_wrapper")

            // Equal gaps between the lines that the title and the score sit between.
            description.setAttribute("android:layout_marginTop", "4.0dip")
            flair.setAttribute("android:layout_marginTop", "4.0dip")

            // The score line is moved here at runtime, after the flair.
            wrapper.insertChild(flair.index + 1, "TextView") {
                setAttribute("android:tag", "syncit_card_score")
                setAttribute("android:visibility", "gone")
                setAttribute("android:layout_width", "fill_parent")
                setAttribute("android:layout_height", "wrap_content")
                setAttribute("android:layout_marginTop", "4.0dip")
                setAttribute("android:includeFontPadding", "false")
            }
        }

        val method = postDescriptionFingerprint.method
        val instructions = method.implementation!!.instructions.toList()

        val drawIndex = instructions.indexOfFirst { instruction ->
            val reference = instruction.getReference<MethodReference>()
            reference?.definingClass == "Loc/b;" &&
                    reference.name == "F" &&
                    reference.parameterTypes.isEmpty() &&
                    reference.returnType == "V"
        }
        check(drawIndex >= 0) { "Could not find where the post description is drawn." }

        method.addInstructions(
            drawIndex + 1,
            "invoke-static {p0}, $EXTENSION_CLASS_DESCRIPTOR->split(Landroid/widget/TextView;)V"
        )
    }
}
