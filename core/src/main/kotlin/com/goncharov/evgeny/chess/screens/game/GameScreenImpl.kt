package com.goncharov.evgeny.chess.screens.game

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FillViewport
import com.goncharov.evgeny.chess.consts.*
import com.goncharov.evgeny.chess.controllers.*
import com.goncharov.evgeny.chess.factory.ChessBoardFactory
import com.goncharov.evgeny.chess.factory.GameFactory
import com.goncharov.evgeny.chess.factory.PiecesFactory
import com.goncharov.evgeny.chess.interactors.*
import com.goncharov.evgeny.chess.managers.ResourceManager
import com.goncharov.evgeny.chess.managers.SavedSettingsManager
import com.goncharov.evgeny.chess.navigation.NavigationKey
import com.goncharov.evgeny.chess.navigation.Navigator
import com.goncharov.evgeny.chess.systems.*
import com.goncharov.evgeny.chess.systems.world.wrap.WorldWrapAndDraggedOnSystem
import com.goncharov.evgeny.chess.systems.world.wrap.WorldWrapInteractorImpl
import com.goncharov.evgeny.chess.ui.game.GameStageImpl
import com.goncharov.evgeny.chess.utils.clearScreen
import com.goncharov.evgeny.chess.utils.debug

class GameScreenImpl(
    private val batch: SpriteBatch,
    resourceManager: ResourceManager,
    savedSettingsManager: SavedSettingsManager,
    private val navigator: Navigator
) : ScreenAdapter(), GameScreen {

    private val viewport = FillViewport(WORLD_WIDTH, WORLD_HEIGHT)
    private val hudViewport = FillViewport(UI_WIDTH, UI_HEIGHT)
    private val gameStage = GameStageImpl(
        batch,
        hudViewport,
        resourceManager,
        this
    )
    private val engine = Engine()
    private val chessBoardFactory =
        ChessBoardFactory(
            engine,
            savedSettingsManager,
            resourceManager[UI_ASSET_DESCRIPTOR],
            resourceManager
        )
    private val piecesFactory = PiecesFactory(engine, resourceManager)
    private val gameFactory = GameFactory(savedSettingsManager, engine)
    private val changeOfMovingController: ChangeOfMovingController =
        ChangeOfMovingControllerImpl(gameStage, savedSettingsManager, engine)
    private val gameInteractor: GameInteractor = GameInteractorImpl(engine)
    private val gameOverController: GameOverController = GameOverControllerImpl(
        engine,
        gameStage
    )
    private val dropInteractor = DropInteractorImpl()

    override fun show() {
        debug(TAG, "show()")
        gameFactory.initialGame()
        Gdx.input.inputProcessor = gameStage
        chessBoardFactory.buildChessBoard()
        chessBoardFactory.addBackground()
        piecesFactory.buildWhitePiecesPlayer()
        piecesFactory.buildBlackPiecesPlayer()
        engine.addSystem(
            WorldWrapAndDraggedOnSystem(
                viewport,
                WorldWrapInteractorImpl(),
                gameInteractor
            )
        )
        engine.addSystem(DragSystem(viewport))
        engine.addSystem(CalculationSystem(viewport, dropInteractor))
        engine.addSystem(MovingSystem(dropInteractor, changeOfMovingController, gameInteractor))
        engine.addSystem(
            RemovePiecesSystem(
                dropInteractor,
                changeOfMovingController,
                gameInteractor,
                gameOverController
            )
        )
        engine.addSystem(PutInPlaceSystem())
        engine.addSystem(RenderSystem(viewport, batch))
        changeOfMovingController.initMessageMoving()
    }

    override fun render(delta: Float) {
        clearScreen()
        engine.update(delta)
        gameStage.act(delta)
        gameStage.draw()
    }

    override fun resize(width: Int, height: Int) {
        debug(TAG, "resize()")
        viewport.update(width, height, true)
        hudViewport.update(width, height, true)
    }

    override fun hide() {
        dispose()
    }

    override fun dispose() {
        debug(TAG, "dispose()")
        gameStage.dispose()
        Gdx.input.inputProcessor = null
    }

    override fun goToTheMainMenu() {
        navigator.navigation(NavigationKey.MainMenuScreenKey)
    }

    companion object {
        private const val TAG = "GameScreen"
    }
}