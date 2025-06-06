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
import * as React from "react";

import { ChartPie } from "@patternfly/react-charts";
import { PieBaseChart } from "./PieBaseChart";

export class PieChart extends PieBaseChart {
  render() {
    const { width, height, themeColor } = this.props;
    return (
      <ChartPie
        ariaDesc={this.props.ariaDescription}
        ariaTitle={this.props.ariaTitle}
        constrainToVisibleArea={true}
        data={this.dataSetToPieChart()}
        labels={({ datum }) => `${datum.name}: ${datum.y}`}
        legendData={this.legendData}
        legendOrientation={this.legendOrientation}
        legendPosition={this.pieLegendPosition()}
        animate={this.animationProp}
        padding={this.props.padding}
        themeColor={themeColor}
        width={width}
        height={height}
      />
    );
  }
}
