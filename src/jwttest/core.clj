(ns jwttest.core
  (:import (org.eclipse.jetty.server Server))
  (:import (org.eclipse.jetty.servlet ServletContextHandler ServletHolder ServletHandler))
  (:import (org.eclipse.jetty.server.handler ContextHandler))
  (:import (org.eclipse.jetty.server.nio SelectChannelConnector))
  (:import (eu.webtoolkit.jwt WApplication WEnvironment WtServlet WText WPushButton WLineEdit WBreak Signal$Listener Signal1$Listener)))

(defn make-hello-app [env]
  (let [wapp (WApplication. env)
        root (.getRoot wapp)
        line-edit (WLineEdit.)
        button (WPushButton. "Greet")
        results-text (WText. "")]

    (.setTitle wapp "Hello world")
    (.addWidget root line-edit)
    (.addWidget root button)
    (.addWidget root (WBreak.))
    (.addWidget root results-text)

    ;; Events!
    (.. button clicked
        (addListener wapp
                     (proxy [Signal1$Listener] []
                       (trigger [ mouse-event ]
                         (.setText results-text (.getText line-edit))))))

    (.. line-edit enterPressed
        (addListener
         wapp
         (proxy [Signal$Listener] []
           (trigger []
             (.setText results-text (.getText line-edit))))))
    wapp))

;; Define a servlet
(def servlet
  (proxy [WtServlet] []
    (createApplication [env]
      (make-hello-app env))))

;;; Current server
(def current-server (atom nil))

(defn shutdown []
  (when @current-server
    (.stop @current-server)
    (reset! current-server nil)))

(defn create-jetty-server []
  (let [connector (doto (SelectChannelConnector.)
                    (.setPort 8080)
                    (.setHost "localhost"))
        server (doto (Server.)
                 (.addConnector connector)
                 (.setSendDateHeader true))]
    server))

(defn startup []
  (shutdown)
  (let [server (create-jetty-server)
        handler (ServletContextHandler. ServletContextHandler/SESSIONS)]
    (.setContextPath handler "/foo")
    (.addServlet handler (ServletHolder. servlet) "/bar/*")
    (.setHandler server handler)
    (.start server)
    (reset! current-server server)))
