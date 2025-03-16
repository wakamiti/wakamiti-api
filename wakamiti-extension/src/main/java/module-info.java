/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
module wakamiti.extension {

    exports es.wakamiti.extension.annotation;
    exports es.wakamiti.extension.manifest;
    exports es.wakamiti.extension;

    uses es.wakamiti.extension.ExtensionManager;
    uses es.wakamiti.extension.InjectionProvider;
    uses es.wakamiti.extension.ModuleLayerProvider;
    uses es.wakamiti.extension.ExtensionLoader;

}