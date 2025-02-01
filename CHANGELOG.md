# Change Log

All notable changes to this project will be documented in this file. This change log follows the conventions
of [keepachangelog.com](http://keepachangelog.com/).

## Unreleased

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

