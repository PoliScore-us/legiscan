package us.poliscore.legiscan;

import java.io.File;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import lombok.SneakyThrows;

public class PoliscoreLegiscanUtil {
private static File deployPath = null;
	
	/**
	 * Calculates and returns the deployed path of the currently running application. If we are deployed inside a container, this will return $CATALINA_HOME/webapps/$CONTEXT_PATH
	 *	 as a resolved absolute path. If we are running inside a jar, this will return the directory that contains the running jar.
	 * 
	 * @return An absolute file path of the deployed application path.
	 */
	public static File getDeployedPath()
	{
		if (deployPath != null)
		{
			return deployPath;
		}
		
		String sDeployPath;
		
		URL rootPath = PoliscoreLegiscanUtil.class.getResource("/");
		if (rootPath != null && !rootPath.getPath().equals(""))
		{
			sDeployPath = rootPath.getPath();
		}
		else
		{
			// If our code lives inside a jar, getResource will return null
			String path = (new PoliscoreLegiscanUtil()).getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
			
			if (path.endsWith(".jar") || path.endsWith(".war") || path.endsWith(".class"))
			{
				path = new File(path).getParent();
			}
			
			sDeployPath = path.replace((new PoliscoreLegiscanUtil()).getClass().getPackage().getName().replace(".", "/"), "");
		}
		
		if (sDeployPath.endsWith("/"))
		{
			sDeployPath = sDeployPath.substring(0, sDeployPath.length() - 1);
		}
		
		if (sDeployPath.endsWith("WEB-INF/classes"))
		{
			sDeployPath = sDeployPath.replace("WEB-INF/classes", "");
		}
		
		if (sDeployPath.endsWith("/classes"))
		{
			sDeployPath = sDeployPath.replace("/classes", "");
		}

		// getPath returns spaces as %20 for some reason
		sDeployPath = sDeployPath.replace("%20", " ");
		
		deployPath = new File(sDeployPath);
		return deployPath;
	}
	
	public static List<File> allFilesWhere(File parent, Predicate<File> criteria)
	{
		List<File> all = new ArrayList<File>();
		
		if (!parent.isDirectory()) return all;
		
		for (File child : parent.listFiles())
		{
			if (child.isDirectory())
			{
				all.addAll(allFilesWhere(child, criteria));
			}
			else if (criteria.test(child))
			{
				all.add(child);
			}
		}
		
		return all;
	}
	
	@SneakyThrows
    public static File childWithName(File root, String target) {
    	File[] result = new File[] { null };
    	
        Files.walkFileTree(root.toPath(), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (target.equals(dir.getFileName().toString())) {
                    result[0] = dir.toFile();
                    return FileVisitResult.TERMINATE;
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return result[0];
    }
}
