package de.jensklingenberg.sheasy.web.ui.apps


import components.materialui.CircularProgress
import components.materialui.FormControl
import components.materialui.Menu
import components.materialui.MenuItem
import components.materialui.Paper
import de.jensklingenberg.sheasy.model.Error
import de.jensklingenberg.sheasy.web.components.materialui.Input
import de.jensklingenberg.sheasy.web.data.FileDataSource
import de.jensklingenberg.sheasy.web.data.NetworkPreferences
import de.jensklingenberg.sheasy.web.data.repository.FileRepository
import de.jensklingenberg.sheasy.web.model.SourceItem
import de.jensklingenberg.sheasy.web.model.render
import de.jensklingenberg.sheasy.web.model.response.App
import de.jensklingenberg.sheasy.model.Status
import de.jensklingenberg.sheasy.web.network.ReactHttpClient
import de.jensklingenberg.sheasy.web.ui.common.BaseComponent
import de.jensklingenberg.sheasy.web.ui.common.styleProps
import de.jensklingenberg.sheasy.web.ui.common.toolbar
import de.jensklingenberg.sheasy.web.usecase.MessageUseCase
import kotlinx.html.InputType
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventTarget
import react.RBuilder
import react.RProps
import react.RState
import react.dom.div
import react.setState


interface AppsViewState : RState {
    var errorMessage: String
    var status: Status
    var openMenu: Boolean
    var anchor: EventTarget?
    var selectedApp: App?
    var item: List<SourceItem>

}


class AppsView : BaseComponent<RProps, AppsViewState>(), AppsContract.View {


    val appsDataSource: FileDataSource = FileRepository(ReactHttpClient(NetworkPreferences()))

    val messageUseCase = MessageUseCase()

    private var presenter: AppsContract.Presenter = AppsPresenter(this, appsDataSource)


    /****************************************** React Lifecycle methods  */


    override fun AppsViewState.init() {
        status = Status.LOADING
        openMenu = false
        anchor = null
        item = emptyList()


    }

    override fun componentDidMount() {
        presenter.getApps()
    }

    override fun RBuilder.render() {

        setupSearchBar(this)
        state.item.render(this)
        Paper {
            attrs {
                elevation = 1
            }




        }
        div {
            CircularProgress {
                attrs {
                    styleProps(display = progressVisibiity())
                }
            }
        }

        messageUseCase.showErrorSnackbar(this, state.errorMessage, snackbarVisibility())
        setupContextMenu(this)

    }

    private fun setupContextMenu(rBuilder: RBuilder) {
        rBuilder.run {
            div {
                Menu {
                    attrs {
                        id = "simple-menu"
                        open = state.openMenu
                        anchorEl = state.anchor
                        onClose = {
                            run {
                                setState {
                                    openMenu = false
                                }
                            }
                        }


                    }
                    MenuItem {

                        +"Download"
                        attrs {
                            styleProps(textAlign = "right")
                            onClick = {
                                presenter.downloadApk(state.selectedApp)
                            }
                        }

                    }
                }
            }

        }
    }


    private fun setupSearchBar(rBuilder: RBuilder) {
        rBuilder.run {
            FormControl {
                Input {
                    attrs {
                        type = InputType.search.realValue
                        name = "search"
                        placeholder = "SEARCH HERE"
                        onChange = {
                            presenter.onSearch((it.target as HTMLInputElement).value)
                        }
                    }
                }
            }
        }
    }


    /****************************************** Presenter methods  */

    override fun showError(error: Error) {
        setState {
            when (error) {
                Error.NetworkError() -> {
                    status = Status.ERROR
                    state.errorMessage = "No Connection"
                }
                Error.NotAuthorizedError() -> {
                    status = Status.ERROR
                    state.errorMessage = "Device is not authorized"
                }
                else -> {
                }
            }
        }

    }

    override fun setData(apps: List<AppSourceItem>) {
        setState {
            status = Status.SUCCESS
            item = apps
        }
    }


    /****************************************** Class methods  */
    override fun handleClickListItem(event: Event, app: App) {
        val currentTarget = event.currentTarget
        setState {
            openMenu = !openMenu
            anchor = currentTarget
            selectedApp = app
        }

    }


    private fun snackbarVisibility(): Boolean {
        return when (state.status) {
            Status.ERROR -> {
                true
            }
            else -> {
                false
            }
        }
    }

    private fun progressVisibiity(): String {
        return when (state.status) {
            Status.LOADING -> {
                ""
            }
            Status.SUCCESS, Status.ERROR -> {
                "none"
            }
        }
    }

    fun handleMenuItemClick(event: Event) {
        setState {
            openMenu = false
            anchor = null
        }
    }


}


fun RBuilder.appsView() = child(AppsView::class) {}