(ns http-client.models.targets
  (:require [schema.core :as s]))

(def targets
  {s/Keyword s/Str})

(s/defschema Targets
  targets)
