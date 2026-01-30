# Change Log

All notable changes to this project will be documented in this file. This change log follows the conventions
of [keepachangelog.com](http://keepachangelog.com/).

## 3.2.1 - 2026-01-30

### Added

- Added a version that implements `hato` under the hood.

## 2.2.1 - 2026-01-01

### Added

- Added support for multiple HTTP targets, allowing requests to be routed to different base URIs.
- Added schema validation for the `targets` configuration to improve reliability and error reporting.

### Changed

- Updated project version and refreshed several dependencies to their latest versions.
- Refactored the core HTTP client to use a `target` and `endpoint` instead of a single `url`.
- Improved code quality by adding linter ignores for `method->request-fn` map.

## 1.2.1 - 2025-02-01

### Added

- Added metrics about the response time of the out requests (`:http-request-response-timing`).

### Changed

- Bump dependencies.

## 1.1.1 - 2025-01-31

### Fixed

- Fixed a bug where the metrics were not being sent with the proper status code response label.
- Refactor the way the metrics are being produced.

## 1.1.0 - 2025-01-07

### Changed

- Use `http-kit` instead of `clj-http`.

## 0.1.0 - 2024-11-15

### Added

- Initial release, first version with basic features.

