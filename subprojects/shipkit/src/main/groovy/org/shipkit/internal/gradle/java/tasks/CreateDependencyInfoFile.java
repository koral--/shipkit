package org.shipkit.internal.gradle.java.tasks;

import org.gradle.api.artifacts.*;
import org.shipkit.internal.gradle.util.StringUtil;
import org.shipkit.internal.notes.util.IOUtil;

import java.util.SortedSet;
import java.util.TreeSet;

public class CreateDependencyInfoFile {

    public static final String SEPARATOR = ":";
    public static final String NEWLINE = "\r\n";
    public static final String DEPENDENCY_INDENT = NEWLINE + " - ";
    public static final String ARTIFACT_INDENT = NEWLINE + "   - ";

    private static final String DESCRIPTION = "This file was generated by Shipkit Gradle plugin. " +
        "It contains all declared dependencies of the project. See http://shipkit.org." + NEWLINE +
        "The format of dependencies is: group:name:version" + NEWLINE +
        "Each of the dependencies may contain artifacts (more nested) formatted like: artifactName:classifier:type:extension";

    public void createDependencyInfoFile(CreateDependencyInfoFileTask task) {
        String result = "# Description" + NEWLINE
            + DESCRIPTION + NEWLINE + NEWLINE
            + "# Dependencies";

        //sorting dependencies to assure that they are always in the same order
        //without depending on Gradle implementation
        SortedSet<String> dependencies = new TreeSet<String>();
        for (Dependency dependency: task.getConfiguration().getAllDependencies()) {
            if (dependency instanceof ModuleDependency) {
                String dep = getResolvedDependency(task, (ModuleDependency) dependency);
                dependencies.add(dep);
            }
        }

        result += DEPENDENCY_INDENT + StringUtil.join(dependencies, DEPENDENCY_INDENT);

        IOUtil.writeFile(task.getOutputFile(), result.toString());
    }

    private String getResolvedDependency(CreateDependencyInfoFileTask task, ModuleDependency dependency) {
        String result = getDependency(task, dependency);
        if (!dependency.getArtifacts().isEmpty()) {
            //sorting artifacts to assure that they are always in the same order
            //without depending on Gradle implementation
            SortedSet<String> artifacts = new TreeSet<String>();
            for (DependencyArtifact artifact : dependency.getArtifacts()) {
                artifacts.add(getArtifact(artifact));
            }
            return result + ARTIFACT_INDENT + StringUtil.join(artifacts, ARTIFACT_INDENT);
        }
        return result;
    }

    private boolean isSubmodule(CreateDependencyInfoFileTask task, ModuleDependency dependency) {
        return task.getProjectGroup().equals(dependency.getGroup())
            && task.getProjectVersion().equals(dependency.getVersion());
    }

    private String getDependency(CreateDependencyInfoFileTask task, ModuleDependency dependency) {
        String result =  dependency.getGroup() + SEPARATOR + dependency.getName();
        if (!isSubmodule(task, dependency)) {
            result += SEPARATOR + dependency.getVersion();
        }
        return result;
    }

    private String getArtifact(DependencyArtifact artifact) {
        return artifact.getName() + SEPARATOR +
            artifact.getClassifier() + SEPARATOR +
            artifact.getType() + SEPARATOR +
            artifact.getExtension();
    }

}
