(ns puppetserver-memmeasure.scenarios.single-catalog-compile
  (:require [puppetserver-memmeasure.scenario :as scenario]
            [puppetserver-memmeasure.schemas :as memmeasure-schemas]
            [puppetserver-memmeasure.util :as util]
            [puppetlabs.services.jruby.jruby-puppet-schemas :as jruby-schemas]
            [schema.core :as schema]
            [me.raynes.fs :as fs]
            [clojure.tools.logging :as log])
  (:import (java.io File)
           (com.puppetlabs.puppetserver JRubyPuppet)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Private

(schema/defn ^:always-validate run-single-catalog-compile-step
  :- memmeasure-schemas/StepRuntimeData
  [jruby-puppet :- JRubyPuppet
   mem-output-run-dir :- File
   step-base-name :- schema/Str
   scenario-context :- memmeasure-schemas/ScenarioContext
   iter :- schema/Int
   _]
  (util/get-catalog jruby-puppet
                    (fs/file mem-output-run-dir
                             (str
                              step-base-name
                              "-"
                              iter
                              "-catalog.json"))
                    "role::by_size::small")
  {:context scenario-context})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Public

(schema/defn ^:always-validate run-single-catalog-compile-scenario
  :- memmeasure-schemas/ScenarioRuntimeData
  "Compile a catalog 'num-catalogs' number of times"
  [num-catalogs :- schema/Int
   jruby-puppet-config :- jruby-schemas/JRubyPuppetConfig
   mem-output-run-dir :- File
   environment-timeout :- memmeasure-schemas/EnvironmentTimeout
   scenario-context :- memmeasure-schemas/ScenarioContext]
  (let [step-base-name (str "single-catalog-compile-env-timeout-"
                            environment-timeout)]
    (util/with-jruby-puppet
     jruby-puppet
     jruby-puppet-config
     (log/infof "Using environment timeout: %s"
                (.getSetting jruby-puppet
                             "environment_timeout"))
     (scenario/run-scenario-body-over-steps
      (partial run-single-catalog-compile-step
               jruby-puppet
               mem-output-run-dir
               step-base-name)
      step-base-name
      mem-output-run-dir
      scenario-context
      (range num-catalogs)))))
