package de.jensklingenberg.sheasy.web.ui.home

import de.jensklingenberg.sheasy.web.model.SourceItem
import de.jensklingenberg.sheasy.web.model.render
import de.jensklingenberg.sheasy.web.ui.common.BaseComponent
import react.RBuilder
import react.RProps
import react.RState
import react.setState


interface HomeViewState : RState {
    var item: List<SourceItem>

}

class HomeView : BaseComponent<RProps, HomeViewState>(), HomeContract.View {


    var presenter = HomePresenter(this)


    override fun HomeViewState.init() {
        item = emptyList()

    }

    override fun componentDidMount() {
        presenter.componentDidMount()
    }

    override fun setData(items: List<SourceItem>) {
        setState {
            this.item = items
        }
    }


    override fun RBuilder.render() {
        //toolbar()

        state.item.render(this)
    }
}


fun RBuilder.app() = child(HomeView::class) {}




