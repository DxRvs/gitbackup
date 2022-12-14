/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package gitbackup;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;

public class Main {
	private static final String name = "gitbackup";
	private static final String optionUrl = "url";
	private static final String optionUser = "login";
	private static final String optionPassord = "password";
	private static final String optionFolder = "folder";

	public static void cloneRepo(String repoUrl, String name, File folder, UsernamePasswordCredentialsProvider cred)
			throws IOException {
		if (folder.exists())
			if (folder.isDirectory()) {
				File pfolder = new File(folder.getAbsolutePath() + File.separator + name);
				pfolder.mkdir();
				try {
					System.out.println("clone project "+name+ " to "+pfolder.getAbsolutePath());
					CloneCommand git = Git.cloneRepository().setURI(repoUrl).setDirectory(pfolder);
					if (cred != null) {
						git.setCredentialsProvider(cred);
					}
					git.call();
				} catch (InvalidRemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TransportException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (GitAPIException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	}

	public static void main(String[] args) {
		
		Options options = new Options();
		Option url = new Option("u", optionUrl, true, "url of gitlab server");
		url.setRequired(true);
		Option user = new Option("l",optionUser,true, "user login");
		user.setRequired(true);
		Option password = new Option("p",optionPassord, true,"user password");
		password.setRequired(true);
		Option folder = new Option("f", optionFolder, true, "folder for save repositories");
		folder.setRequired(true);

		options.addOption(url);
		options.addOption(user);
		options.addOption(password);
		options.addOption(folder);
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp(name, options);
			System.exit(1);
		}
		String gitlabUrl = cmd.getOptionValue(optionUrl);
		String backupPath = cmd.getOptionValue(optionFolder);
		String pass = cmd.getOptionValue(optionPassord);
		String uname = cmd.getOptionValue(optionUser);
		
		try {
		
			GitLabApi gitLabApi = GitLabApi.oauth2Login(gitlabUrl, uname, pass);
			List<Project> projects = gitLabApi.getProjectApi().getProjects();
			System.out.println("Total projects:" +projects.size());
			UsernamePasswordCredentialsProvider cred = new UsernamePasswordCredentialsProvider(uname, pass);
			for (Project p : projects) {
				String gitUrl= p.getHttpUrlToRepo();
				String pName = p.getNamespace().getName().strip().replace(" ", "")+File.separator +p.getName();
				cloneRepo(gitUrl, pName, new File(backupPath), cred);
			}
		} catch (GitLabApiException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(1);
		}

		System.out.println("end");
	}
}
