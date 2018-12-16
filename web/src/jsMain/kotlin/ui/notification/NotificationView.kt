package ui.notification


import components.Notification.Notification
import components.Notification.ReactNotificationOptions
import components.Notification.defaultReactNotificationOptions
import components.reactstrap.Button
import components.reactstrap.Tooltip
import react.*


interface NotificationVState : RState {
    var openToolTip: Boolean
    var ignoreNotification: Boolean
    var notiOptions: ReactNotificationOptions
    var notiTitle: String
}


class NotificationView : RComponent<RProps, NotificationVState>(), NotificationContract.View {
    private var presenter: NotificationContract.Presenter? = null


    override fun NotificationVState.init(props: RProps) {
        openToolTip = true
        ignoreNotification = true
        notiOptions = defaultReactNotificationOptions
        notiTitle = ""
    }

    override fun componentDidMount() {
        presenter = NotificationPresenter(this)
        presenter?.componentDidMount()
    }


    private fun handleChange() {
        setState {
            openToolTip = !openToolTip
        }
    }


    override fun showNotification(reactNotificationOptions: ReactNotificationOptions) {

        setState {
            notiTitle = reactNotificationOptions.title ?: ""
            notiOptions = reactNotificationOptions
            console.log("HKjldfjkslajflajslkjdfs" + ignoreNotification)
            this.ignoreNotification = false
        }

    }


    override fun RBuilder.render() {
        Button {
            +"HHH"
            attrs {
                id = "HALLOID"
            }
        }

        Tooltip {
            +"HHHH"
            attrs {
                placement = "top"
                target = "HALLOID"
                toggle = { handleChange() }
                isOpen = this@NotificationView.state.openToolTip
            }
        }

        Notification {
            attrs {
                title = state.notiTitle
                timeout = 5000
                options = state.notiOptions

                console.log(this@NotificationView.state.ignoreNotification)
            }
        }
    }

}

fun RBuilder.NotificationView() = child(NotificationView::class) {}