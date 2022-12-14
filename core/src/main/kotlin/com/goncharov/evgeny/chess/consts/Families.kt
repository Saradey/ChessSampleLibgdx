package com.goncharov.evgeny.chess.consts

import com.badlogic.ashley.core.Family
import com.goncharov.evgeny.chess.components.*

val gameFamily: Family = Family.all(GameComponent::class.java).get()

val piecesFamily: Family = Family.all(PiecesComponent::class.java).get()

val cellsFamily: Family = Family.all(CellComponent::class.java).get()

val removedPiecesFamily: Family = Family.all(RemovedPiecesComponent::class.java).get()

val allPiecesAndCells: Family = Family.all(SpriteComponent::class.java).one(
    PiecesComponent::class.java,
    CellComponent::class.java,
    RemovedPiecesComponent::class.java
).get()

val renderFamily: Family = Family.all(
    SpriteComponent::class.java,
    LayerComponent::class.java
).get()

val draggedFamily: Family = Family.all(
    DraggedComponent::class.java,
    PiecesComponent::class.java
).get()