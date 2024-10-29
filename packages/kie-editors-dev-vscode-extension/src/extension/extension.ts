/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import { backendI18nDefaults, backendI18nDictionaries } from "@kie-tools-core/backend/dist/i18n";
import { VsCodeBackendProxy } from "@kie-tools-core/backend/dist/vscode";
import { EditorEnvelopeLocator, EnvelopeContentType, EnvelopeMapping } from "@kie-tools-core/editor/dist/api";
import { I18n } from "@kie-tools-core/i18n/dist/core";
import * as KogitoVsCode from "@kie-tools-core/vscode-extension";
import * as vscode from "vscode";
import { generateFormsCommand } from "@kie-tools/form-code-generator-vscode-command/dist/generateFormCodeCommand";

let backendProxy: VsCodeBackendProxy;

export async function activate(context: vscode.ExtensionContext) {
  console.info("Extension is alive.");

  const backendI18n = new I18n(backendI18nDefaults, backendI18nDictionaries, vscode.env.language);
  backendProxy = new VsCodeBackendProxy(context, backendI18n);

  KogitoVsCode.startExtension({
    extensionName: "kie-group.kie-editors-dev-vscode-extension",
    context: context,
    viewType: "kieKogitoWebviewEditors",
    generateSvgCommandId: "extension.kogito.getPreviewSvg",
    silentlyGenerateSvgCommandId: "extension.kogito.silentlyGenerateSvg",
    editorEnvelopeLocator: new EditorEnvelopeLocator("vscode", [
      new EnvelopeMapping({
        type: "bpmn",
        filePathGlob: "**/*.bpmn?(2)",
        resourcesPathPrefix: "dist/webview/editors/bpmn",
        envelopeContent: { type: EnvelopeContentType.PATH, path: "dist/webview/BpmnEditorEnvelopeApp.js" },
      }),
      new EnvelopeMapping({
        type: "dmn",
        filePathGlob: "**/*.dmn",
        resourcesPathPrefix: "dist/webview/editors/dmn",
        envelopeContent: { type: EnvelopeContentType.PATH, path: "dist/webview/DmnEditorEnvelopeApp.js" },
      }),
      new EnvelopeMapping({
        type: "scesim",
        filePathGlob: "**/*.scesim",
        resourcesPathPrefix: "dist/webview/editors/scesim",
        envelopeContent: { type: EnvelopeContentType.PATH, path: "dist/webview/SceSimEditorEnvelopeApp.js" },
      }),
      new EnvelopeMapping({
        type: "pmml",
        filePathGlob: "**/*.pmml",
        resourcesPathPrefix: "dist/webview/editors/pmml",
        envelopeContent: { type: EnvelopeContentType.PATH, path: "dist/webview/PMMLEditorEnvelopeApp.js" },
      }),
    ]),
    backendProxy: backendProxy,
  });

  context.subscriptions.push(
    vscode.commands.registerCommand("extension.apache.kie.kogitoEditors.generateFormCode", async (args: any) =>
      generateFormsCommand()
    )
  );

  console.info("Extension is successfully setup.");
}

export function deactivate() {
  backendProxy?.stopServices();
}
