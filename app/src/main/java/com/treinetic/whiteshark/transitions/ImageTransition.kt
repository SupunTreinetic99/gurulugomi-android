package com.treinetic.whiteshark.transitions

import androidx.transition.ChangeBounds
import androidx.transition.ChangeImageTransform
import androidx.transition.ChangeTransform
import androidx.transition.TransitionSet

/**
 * Created by Nuwan on 2/7/19.
 */
class ImageTransition : TransitionSet() {

    init {
        setOrdering(ORDERING_TOGETHER)
        addTransition(ChangeBounds())
            .addTransition(ChangeTransform())
            .addTransition(ChangeImageTransform())
    }
}