package app.syncit.patches.sync.layout

import app.morphe.patcher.patch.patch
import app.morphe.patcher.patch.resourceXml
import app.syncit.patches.sync.Constants.SYNC_FOR_REDDIT

@Suppress("unused")
val hideExpandButtonPatch = patch(
    name = "Hide the card dropdown button",
    description = "Hides the expand chevron on the right side of each post in the list, compact " +
            "and smaller card layouts."
) {
    compatibleWith(SYNC_FOR_REDDIT)

    execute {
        listOf(
            "res/layout/holder_smaller_cards.xml",
            "res/layout/holder_compact.xml",
            "res/layout/holder_list.xml",
        ).forEach { path ->
            resourceXml(path).use { layout ->
                layout.elementByAttribute("android:id", "@id/holder_list_expand")
                    .setAttribute("android:visibility", "gone")
            }
        }
    }
}
