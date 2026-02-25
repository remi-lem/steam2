package org.steam2.client.application

import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import org.steam2.client.daos.EditeurDAO
import org.steam2.client.daos.JeuVideoDAO
import org.steam2.client.daos.JoueurDAO

class Menus (
    private val gui: MultiWindowTextGUI,
    private val joueurDAO: JoueurDAO,
    private val jeuVideoDAO: JeuVideoDAO,

) {
}