# play-mail plugin

This module for Play! Framework 1 applications allows sending emails with Freemarker templating.

# How to use

####  Add the dependency to your `dependencies.yml` file

```
require:
    - mail -> mail 0.1

repositories:
    - mail:
        type:       http
        artifact:   "http://release.sismics.com/repo/play/[module]-[revision].zip"
        contains:
            - mail -> *

```
require:
    - mail -> mail 0.1
```
####  Add the routes to your `routes` file

```
# Secure routes
*       /               module:mail
```

# License

This software is released under the terms of the Apache License, Version 2.0. See `LICENSE` for more
information or see <https://opensource.org/licenses/Apache-2.0>.
