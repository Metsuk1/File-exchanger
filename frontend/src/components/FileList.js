import React, { useState, useEffect } from 'react';
import { getFiles, uploadFile, downloadFile, deleteFile, shareFile } from '../services/api';

function FileList() {
    const [files, setFiles] = useState([]);
    const [uploading, setUploading] = useState(false);
    const [shareLink, setShareLink] = useState('');
    const [showShareModal, setShowShareModal] = useState(false);

    useEffect(() => {
        fetchFiles();
    }, []);

    const fetchFiles = async () => {
        try {
            const response = await getFiles();
            setFiles(response.data);
        } catch (err) {
            console.error('Failed to load files');
        }
    };

    const handleUpload = async (e) => {
        const file = e.target.files[0];
        if (!file) return;
        setUploading(true);
        try {
            await uploadFile(file);
            fetchFiles();
        } catch (err) {
            alert('Failed of loading');
        } finally {
            setUploading(false);
            e.target.value = '';
        }
    };

    const handleDownload = async (fileId, fileName) => {
        try {
            const response = await downloadFile(fileId);
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', fileName);
            document.body.appendChild(link);
            link.click();
            link.remove();
        } catch (err) {
            alert('Error downloading');
        }
    };

    const handleShare = async (fileId) => {
        try {
            const response = await shareFile(fileId);
            const publicLink = window.location.origin + response.data.link;
            setShareLink(publicLink);
            setShowShareModal(true);
        } catch (err) {
            alert('Error creating share link');
        }
    };

    const handleCopyLink = () => {
        navigator.clipboard.writeText(shareLink).then(() => {
            alert('Link copied to clipboard!');
        });
    };

    const handleDelete = async (fileId) => {
        if (!window.confirm('Delete file?')) return;
        try {
            await deleteFile(fileId);
            setFiles(files.filter(f => f.id !== fileId));
        } catch (err) {
            alert('Error deleting');
        }
    };

    return (
        <div className="files-container">
            <div className="upload-section">
                <label className="file-input-label">
                    {uploading ? 'Loading...' : 'Choose file'}
                    <input type="file" onChange={handleUpload} disabled={uploading} />
                </label>
            </div>

            <ul className="file-list">
                {files.length === 0 ? (
                    <p className="no-files">No loaded files</p>
                ) : (
                    files.map(file => (
                        <li key={file.id} className="file-item">
                            <div className="file-info">
                                <strong>{file.fileName}</strong>
                                <span>{(file.size / 1024).toFixed(2)} KB</span>
                            </div>
                            <div className="file-actions">
                                <button onClick={() => handleDownload(file.id, file.fileName)} className="btn-small">
                                    Download
                                </button>
                                <button onClick={() => handleShare(file.id)} className="btn-share">
                                    Share
                                </button>
                                <button onClick={() => handleDelete(file.id)} className="btn-danger">
                                    Delete
                                </button>
                            </div>
                        </li>
                    ))
                )}
            </ul>

            {showShareModal && (
                <div className="share-modal-overlay" onClick={() => setShowShareModal(false)}>
                    <div className="share-modal" onClick={e => e.stopPropagation()}>
                        <h3>Share Link</h3>
                        <div className="share-link-box">
                            <input type="text" value={shareLink} readOnly />
                            <button onClick={handleCopyLink} className="btn-copy">Copy</button>
                        </div>
                        <button onClick={() => setShowShareModal(false)} className="btn-close-modal">Close</button>
                    </div>
                </div>
            )}
        </div>
    );
}

export default FileList;
