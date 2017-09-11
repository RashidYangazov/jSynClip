/*
Sample Description from
https://stackoverflow.com/questions/31388036/how-to-handle-incoming-files-in-apache-mina-sshd-sftp-server-in-java
 */
package ru.yangazov.jsynclip;

import java.nio.file.CopyOption;
import java.nio.file.Path;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.*;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.subsystem.sftp.DirectoryHandle;
import org.apache.sshd.server.subsystem.sftp.FileHandle;
import org.apache.sshd.server.subsystem.sftp.Handle;
import org.apache.sshd.server.subsystem.sftp.SftpEventListener;

/**
 *
 * @author Rashid.Yangazov
 */
public class jscSftpEventListener implements SftpEventListener {
    
    private final Logger logger;

    SFTPService service;
 
    
    public jscSftpEventListener(SFTPService service) {
        this.logger = Logger.getLogger(jscSftpEventListener.class.toString());
        this.service = service;
    }

    public interface FileUploadCompleteListener {
        void onFileReady(File file);
    }

    private List<FileUploadCompleteListener> fileReadyListeners = new ArrayList<FileUploadCompleteListener>();

    public void addFileUploadCompleteListener(FileUploadCompleteListener listener) {
        fileReadyListeners.add(listener);
    }

    public void removeFileUploadCompleteListener(FileUploadCompleteListener listener) {
        fileReadyListeners.remove(listener);
    }

    @Override
    public void initialized(ServerSession serverSession, int version) {

    }

    @Override
    public void destroying(ServerSession serverSession) {

    }

    @Override
    public void open(ServerSession serverSession, String remoteHandle, Handle localHandle) {
        File openedFile = localHandle.getFile().toFile();
        if (openedFile.exists() && openedFile.isFile()) {
        }
    }

    @Override
    public void read(ServerSession serverSession, String remoteHandle, DirectoryHandle localHandle, Map<String,Path> entries) {

    }




    @Override
    public void blocking(ServerSession serverSession,  String remoteHandle, FileHandle localHandle, long offset, long length, int mask) {
    }

    @Override
    public void blocked(ServerSession serverSession, String remoteHandle, FileHandle localHandle, long offset, long length, int mask, Throwable thrown) {
    }

    @Override
    public void unblocking(ServerSession serverSession, String remoteHandle, FileHandle localHandle, long offset, long length) {
    }



    @Override
    public void close(ServerSession serverSession, String remoteHandle, Handle localHandle) {
        File closedFile = localHandle.getFile().toFile();
        if (closedFile.exists() && closedFile.isFile()) {
            logger.info(String.format("User %s closed file: \"%s\"", serverSession.getUsername(), localHandle.getFile().toAbsolutePath()));
            this.service.UserWroteFile(serverSession.getUsername(), localHandle.getFile());

            for (FileUploadCompleteListener readyForUploadListener : fileReadyListeners) {
                readyForUploadListener.onFileReady(closedFile);
            }
        }
    }

    @Override
    public void creating(ServerSession serverSession, Path path, Map<String,?> attrs) throws UnsupportedOperationException {
        logger.warning(String.format("Blocked user %s attempt to create a directory \"%s\"", serverSession.getUsername(), path.toString()));
        throw new UnsupportedOperationException("Creating sub-directories is not permitted.");
    }

    @Override
    public void created(ServerSession serverSession, Path path, Map<String,?> attrs, Throwable thrown) {
        String username = serverSession.getUsername();
        logger.info(String.format("User %s created: \"%s\"", username, path.toString()));
        service.UserWroteFile(username, path);
    }

    @Override
    public void moving(ServerSession serverSession, Path path, Path path1, Collection<CopyOption> collection) {

    }

    @Override
    public void moved(ServerSession serverSession, Path source, Path destination, Collection<CopyOption> collection, Throwable throwable) {
        String username = serverSession.getUsername();
        logger.info(String.format("User %s moved: \"%s\" to \"%s\"", username, source.toString(), destination.toString()));
        service.UserWroteFile(username, destination);
    }

    @Override
    public void removing(ServerSession serverSession, Path path) {

    }

    @Override
    public void removed(ServerSession serverSession, Path path, Throwable thrown) {

    }

    @Override
    public void linking(ServerSession serverSession, Path source, Path target, boolean symLink) throws UnsupportedOperationException {
        logger.warning(String.format("Blocked user %s attempt to create a link to \"%s\" at \"%s\"", serverSession.getUsername(), target.toString(), source.toString()));
        throw new UnsupportedOperationException("Creating links is not permitted");
    }

    @Override
    public void linked(ServerSession serverSession, Path source, Path target, boolean symLink, Throwable thrown) {

    }

    @Override
    public void modifyingAttributes(ServerSession serverSession, Path path, Map<String,?> attrs) {

    }

    @Override
    public void modifiedAttributes(ServerSession serverSession, Path path, Map<String,?> attrs, Throwable thrown) {
        String username = serverSession.getUsername();
        service.UserWroteFile(username, path);
    }    
    
}
