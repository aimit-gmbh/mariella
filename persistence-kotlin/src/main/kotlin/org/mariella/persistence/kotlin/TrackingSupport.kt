package org.mariella.persistence.kotlin

import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport
import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty

@Suppress("LeakingThis")
abstract class TrackingSupport {
    protected val propertyChangeSupport = PropertyChangeSupport(this)

    fun <T> changeSupport(initialValue: T): ReadWriteProperty<Any?, T> {
        return Delegates.observable(initialValue) { prop, old, new ->
            propertyChangeSupport.firePropertyChange(prop.name, old, new)
        }
    }

    fun <T> changeSupport(): ReadWriteProperty<Any?, T> {
        @Suppress("UNCHECKED_CAST")
        return changeSupport(null as T)
    }

    fun addPropertyChangeListener(listener: PropertyChangeListener?) {
        propertyChangeSupport.addPropertyChangeListener(listener)
    }

    fun removePropertyChangeListener(listener: PropertyChangeListener?) {
        propertyChangeSupport.removePropertyChangeListener(listener)
    }
}