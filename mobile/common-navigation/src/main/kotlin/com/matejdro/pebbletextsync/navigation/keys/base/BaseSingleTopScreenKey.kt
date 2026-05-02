package com.matejdro.pebbletextsync.navigation.keys.base

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.ui.graphics.TransformOrigin
import si.inova.kotlinova.navigation.screenkeys.SingleTopKey

abstract class BaseSingleTopScreenKey : SingleTopKey() {
   @Suppress("MagicNumber") // Magic numbers are the whole point of this function
   override fun backAnimation(
      scope: AnimatedContentTransitionScope<*>,
      backSwipeEdge: Int?,
   ): ContentTransform {
      val scaleTransformOrigin = when (backSwipeEdge) {
         EDGE_LEFT -> TransformOrigin(pivotFractionX = 1f, pivotFractionY = 0.5f)
         EDGE_RIGHT -> TransformOrigin(pivotFractionX = 0f, pivotFractionY = 0.5f)
         else -> TransformOrigin.Center
      }

      return (
         fadeIn(PredictiveBackFadeAnimationSpec()) +
            scaleIn(initialScale = 1.1f, transformOrigin = scaleTransformOrigin)
         ) togetherWith
         (
            fadeOut(PredictiveBackFadeAnimationSpec()) +
               scaleOut(targetScale = 0.9f, transformOrigin = scaleTransformOrigin)
            )
   }
}
