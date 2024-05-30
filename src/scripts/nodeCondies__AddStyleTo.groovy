/**
 * Copyright (C) 2024  macmarrum (at) outlook (dot) ie
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
// @ExecutionModes({ON_SINGLE_NODE="/menu_bar/Mac1/NodeCondies"})

import io.github.macmarrum.swing.AutoCompletionComboDialog
import org.freeplane.api.Controller
import org.freeplane.api.Node
import org.freeplane.core.ui.components.UITools
import org.freeplane.features.map.MapModel
import org.freeplane.features.map.NodeModel
import org.freeplane.features.styles.IStyle
import org.freeplane.features.styles.MapStyleModel
import org.freeplane.features.styles.StyleTranslatedObject
import org.freeplane.plugin.script.proxy.AConditionalStyleProxy
import org.freeplane.plugin.script.proxy.NodeConditionalStyleProxy

import javax.swing.*

/* NOTE
 * Freeplane can have LOCALIZED_STYLE_REF and STYLE_REF with the same value, e.g. "styles.topic"
 * The first one is Freeplane-internal and translated, the second is user-defined and not translated
 * They differ in IStyle, but Scripting API (node.style.name) cannot tell them apart
 * Therefore it's important to operate on IStyles rather than on their String representations
 */

def node = node as Node
def c = c as Controller

def selectedNodes = c.selecteds

List<IStyle> currentConditionalStyles
if (selectedNodes.size() == 1) {
    def ncsItems = node.conditionalStyles.collect()
    currentConditionalStyles = new ArrayList<IStyle>(ncsItems.size())
    ncsItems.each {
        if (it.active && it.always && !it.last)
            currentConditionalStyles << (it as AConditionalStyleProxy).style
    }
} else {
    currentConditionalStyles = Collections.emptyList()
}

// MapStyleModel::getNodeStyles() contains user-def and automatic-level styles plus DEFAULT and FLOATING
def stylesForComboBox = MapStyleModel.getExtension(node.mindMap.delegate as MapModel).nodeStyles.findAll {
    it !in currentConditionalStyles && it !== MapStyleModel.DEFAULT_STYLE
}
def styleNamesForComboBox = new String[stylesForComboBox.size()]
String displayName
stylesForComboBox.eachWithIndex { style, i ->
    displayName = style as String
    styleNamesForComboBox[i] = style instanceof StyleTranslatedObject ? "$displayName   (${style.object})".toString() : displayName
}

def onEntryAccepted = { JComboBox<String> comboBox ->
    def style = stylesForComboBox[comboBox.selectedIndex]
    selectedNodes.each { Node n ->
        def ncs = n.conditionalStyles
        if (!ncs.find { it.active && it.always && (it as AConditionalStyleProxy).style == style && !it.last }) {
            // use NodeConditionalStyleProxy to add iStyle,
            // otherwise there's a risk that a user-defined style will be added instead of a predefined one
            // if both have the same reference name like styles.topic or AutomaticLayout.level.root
            // -- see NodeStyleProxy::styleByName
            ncs.add(new NodeConditionalStyleProxy(n.delegate as NodeModel, true, null, style, false))
        }
    }
}

SwingUtilities.invokeLater {
    new AutoCompletionComboDialog(UITools.currentFrame, 'Add style', styleNamesForComboBox, onEntryAccepted)
}
