/*
 * SonarQube Java
 * Copyright (C) 2012 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.java.bridges;

import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.PersistenceMode;
import org.sonar.api.measures.RangeDistributionBuilder;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.utils.ParsingUtils;
import org.sonar.java.ast.api.JavaMetric;
import org.sonar.squid.api.SourceCode;
import org.sonar.squid.api.SourceFile;
import org.sonar.squid.api.SourcePackage;
import org.sonar.squid.api.SourceProject;
import org.sonar.squid.measures.Metric;
import org.sonar.squid.measures.MetricDef;

public final class CopyBasicMeasuresBridge extends Bridge {

  private static final Number[] LIMITS = {0, 5, 10, 20, 30, 60, 90};

  @Override
  public void onFile(SourceFile squidFile, Resource sonarResource) {
    copy(squidFile, sonarResource, JavaMetric.LINES_OF_CODE, CoreMetrics.NCLOC);
    copy(squidFile, sonarResource, JavaMetric.LINES, CoreMetrics.LINES);
    copy(squidFile, sonarResource, JavaMetric.COMMENT_LINES_WITHOUT_HEADER, CoreMetrics.COMMENT_LINES);
    copy(squidFile, sonarResource, JavaMetric.STATEMENTS, CoreMetrics.STATEMENTS);
    copy(squidFile, sonarResource, JavaMetric.FILES, CoreMetrics.FILES);
    copy(squidFile, sonarResource, JavaMetric.CLASSES, CoreMetrics.CLASSES);

    copy(squidFile, sonarResource, JavaMetric.COMPLEXITY, CoreMetrics.COMPLEXITY);
    context.saveMeasure(sonarResource, new RangeDistributionBuilder(CoreMetrics.FILE_COMPLEXITY_DISTRIBUTION, LIMITS)
      .add(squidFile.getInt(JavaMetric.COMPLEXITY))
      .build(true)
      .setPersistenceMode(PersistenceMode.MEMORY));

    copy(squidFile, sonarResource, Metric.PUBLIC_API, CoreMetrics.PUBLIC_API);
    context.saveMeasure(sonarResource, CoreMetrics.PUBLIC_DOCUMENTED_API_DENSITY, ParsingUtils.scaleValue(squidFile.getDouble(Metric.PUBLIC_DOCUMENTED_API_DENSITY) * 100, 2));
    double undocumentedApi = squidFile.getDouble(Metric.PUBLIC_API) - squidFile.getInt(Metric.PUBLIC_DOC_API);
    context.saveMeasure(sonarResource, CoreMetrics.PUBLIC_UNDOCUMENTED_API, undocumentedApi);
  }

  private void copy(SourceCode squidResource, Resource sonarResource, MetricDef squidMetric, org.sonar.api.measures.Metric sonarMetric) {
    context.saveMeasure(sonarResource, sonarMetric, squidResource.getDouble(squidMetric));
  }

}
