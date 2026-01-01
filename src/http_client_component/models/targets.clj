(ns http-client-component.models.targets
  (:require [schema.core :as s]))

(def targets
  {(s/optional-key s/Keyword) s/Str})

(s/defschema Targets
  targets)
