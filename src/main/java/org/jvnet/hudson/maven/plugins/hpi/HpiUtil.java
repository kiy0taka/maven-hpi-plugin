package org.jvnet.hudson.maven.plugins.hpi;

import java.io.IOException;
import java.util.Arrays;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.kohsuke.stapler.framework.io.IOException2;

/**
 * @author Kohsuke Kawaguchi
 */
class HpiUtil {
    static boolean isPlugin(Artifact artifact) throws IOException {
        try {
            // some artifacts aren't even Java, so ignore those.
            if(!artifact.getType().equals("jar"))    return false;

            // this can happened with maven 3 and doesn't have any side effect here
            if(artifact.getFile() == null ) return false;
            // could a reactor member in member (mvn test-compile with core :-) )
            if(artifact.getFile().isDirectory()) return false;
            
            JarFile jar = new JarFile(artifact.getFile());
            try {
                Manifest manifest = jar.getManifest();
                if(manifest==null)  return false;
                for( String key : Arrays.asList("Plugin-Class","Plugin-Version")) {
                    if(manifest.getMainAttributes().getValue(key) != null)
                        return true;
                }
                return false;
            } finally {
                jar.close();
            }
        } catch (IOException e) {
            throw new IOException2("Failed to open artifact "+artifact.toString()+" at "+artifact.getFile(),e);
        }
    }

    static String findJenkinsVersion(MavenProject project) {
        for(Dependency a : (List<Dependency>)project.getDependencies()) {
            if((a.getGroupId().equals("org.jenkins-ci.main") || a.getGroupId().equals("org.jvnet.hudson.main"))
            && (a.getArtifactId().equals("jenkins-core") || a.getArtifactId().equals("hudson-core"))) {
                return a.getVersion();
            }
        }
        System.err.println("** Warning: failed to determine Jenkins version this plugin depends on.");
        return null;
    }
}
