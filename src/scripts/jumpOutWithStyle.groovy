// @ExecutionModes({ON_SINGLE_NODE="/menu_bar/Mac1/Jump"})


import org.freeplane.api.ConditionalStyle
import org.freeplane.core.util.FreeplaneVersion
import org.freeplane.core.util.MenuUtils
import org.freeplane.plugin.script.proxy.ScriptUtils

// put the name of your custom style, if different
def jumpInStyleName = 'JumpIn'

ScriptUtils.c().viewRoot.conditionalStyles.findAll {
    it.active && it.always && it.styleName == jumpInStyleName && !it.last
}.each { ConditionalStyle it ->
    it.remove()
}
MenuUtils.executeMenuItems(['JumpOutAction'])
if (FreeplaneVersion.version < new FreeplaneVersion(1, 11, 1)) {
    // force view refresh
    def node = ScriptUtils.node()
    node.style.name = node.style.name
}