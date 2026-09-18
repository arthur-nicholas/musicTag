const themeToggle = document.getElementById('themeToggle');
const sunIcon = document.getElementById('sunIcon');
const moonIcon = document.getElementById('moonIcon');

const savedTheme = localStorage.getItem('theme') || 'dark';
if (savedTheme === 'light') {
    document.body.classList.add('light');
    sunIcon.classList.add('hidden');
    moonIcon.classList.remove('hidden');
}

themeToggle.addEventListener('click', () => {
    document.body.classList.toggle('light');
    const isLight = document.body.classList.contains('light');
    sunIcon.classList.toggle('hidden', isLight);
    moonIcon.classList.toggle('hidden', !isLight);
    localStorage.setItem('theme', isLight ? 'light' : 'dark');
});

const fileInput = document.getElementById('fileInput');
const fileLabelText = document.getElementById('fileLabelText');
const fileInfo = document.getElementById('fileInfo');
const fileName = document.getElementById('fileName');
const currentMetadata = document.getElementById('currentMetadata');
const artistInput = document.getElementById('artistInput');
const titleInput = document.getElementById('titleInput');
const searchBtn = document.getElementById('searchBtn');
const resultsSection = document.getElementById('resultsSection');
const searchResults = document.getElementById('searchResults');
const previewSection = document.getElementById('previewSection');
const previewArt = document.getElementById('previewArt');
const previewTitle = document.getElementById('previewTitle');
const previewArtist = document.getElementById('previewArtist');
const previewAlbum = document.getElementById('previewAlbum');
const previewYear = document.getElementById('previewYear');
const previewGenre = document.getElementById('previewGenre');
const applyBtn = document.getElementById('applyBtn');
const loading = document.getElementById('loading');

let selectedFile = null;
let selectedMetadata = null;

const ALLOWED_TYPES = [
    'audio/mpeg', 'audio/mp3', 'audio/flac', 'audio/ogg',
    'audio/opus', 'audio/wav', 'audio/x-m4a', 'audio/aac',
    'audio/x-wav', 'audio/wave'
];

fileInput.addEventListener('change', async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    const ext = file.name.split('.').pop().toLowerCase();
    const validExts = ['mp3', 'flac', 'ogg', 'opus', 'wav', 'm4a', 'aac'];

    if (!ALLOWED_TYPES.includes(file.type) && !validExts.includes(ext)) {
        alert('Formato de arquivo nao suportado. Use: MP3, FLAC, OGG, OPUS, WAV, M4A, AAC');
        fileInput.value = '';
        return;
    }

    selectedFile = file;
    fileLabelText.textContent = 'Trocar arquivo';
    fileName.textContent = file.name;

    showLoading(true);

    try {
        const formData = new FormData();
        formData.append('file', file);

        const response = await fetch('/api/upload', {
            method: 'POST',
            body: formData
        });

        const data = await response.json();

        if (data.error) {
            alert('Erro ao ler arquivo: ' + data.error);
            showLoading(false);
            return;
        }

        const meta = data.metadata;
        currentMetadata.innerHTML = `
            <strong>Metadados existentes:</strong><br>
            Titulo: ${meta.title || '(vazio)'} |
            Artista: ${meta.artist || '(vazio)'} |
            Album: ${meta.album || '(vazio)'} |
            Ano: ${meta.year || '(vazio)'}
        `;

        if (meta.title) titleInput.value = meta.title;
        if (meta.artist) artistInput.value = meta.artist;

        fileInfo.classList.remove('hidden');
        searchBtn.disabled = false;

    } catch (err) {
        alert('Erro ao enviar arquivo: ' + err.message);
    } finally {
        showLoading(false);
    }
});

searchBtn.addEventListener('click', async () => {
    const artist = artistInput.value.trim();
    const title = titleInput.value.trim();

    if (!artist && !title) {
        alert('Preencha pelo menos o artista ou o titulo da musica');
        return;
    }

    showLoading(true);

    try {
        const params = new URLSearchParams({
            artist: artist,
            title: title
        });

        const response = await fetch(`/api/search?${params}`);
        const results = await response.json();

        searchResults.innerHTML = '';

        if (results.length === 0) {
            searchResults.innerHTML = '<p style="color:#888;text-align:center;">Nenhum resultado encontrado</p>';
            resultsSection.classList.remove('hidden');
            return;
        }

        results.forEach((result, index) => {
            const item = document.createElement('div');
            item.className = 'result-item';
            item.innerHTML = `
                <img class="result-thumb" src="${result.albumArtUrl || ''}" alt="Capa"
                     onerror="this.style.display='none'">
                <div class="result-info">
                    <div class="result-title">${escapeHtml(result.title)}</div>
                    <div class="result-artist">${escapeHtml(result.artist)}</div>
                    <div class="result-artist">${escapeHtml(result.album)}</div>
                </div>
            `;
            item.addEventListener('click', () => selectResult(result, item));
            searchResults.appendChild(item);
        });

        resultsSection.classList.remove('hidden');

    } catch (err) {
        alert('Erro na busca: ' + err.message);
    } finally {
        showLoading(false);
    }
});

function selectResult(result, element) {
    document.querySelectorAll('.result-item').forEach(el => el.classList.remove('selected'));
    element.classList.add('selected');

    selectedMetadata = result;

    previewTitle.value = result.title || '';
    previewArtist.value = result.artist || '';
    previewAlbum.value = result.album || '';
    previewYear.value = result.year || '';
    previewGenre.value = result.genre || '';

    if (result.albumArtUrl) {
        previewArt.src = result.albumArtUrl;
        previewArt.style.display = 'block';
    } else {
        previewArt.style.display = 'none';
    }

    previewSection.classList.remove('hidden');
}

applyBtn.addEventListener('click', async () => {
    if (!selectedFile) {
        alert('Nenhum arquivo selecionado');
        return;
    }

    const metadata = {
        title: previewTitle.value,
        artist: previewArtist.value,
        album: previewAlbum.value,
        year: previewYear.value,
        genre: previewGenre.value,
        trackNumber: '',
        albumArtUrl: selectedMetadata?.albumArtUrl || ''
    };

    showLoading(true);

    try {
        const formData = new FormData();
        formData.append('file', selectedFile);
        formData.append('metadata', JSON.stringify(metadata));

        const response = await fetch('/api/apply', {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            throw new Error('Erro ao processar arquivo');
        }

        const blob = await response.blob();
        const disposition = response.headers.get('Content-Disposition');
        let downloadName = selectedFile.name;
        if (disposition) {
            const match = disposition.match(/filename\*?=(?:UTF-8'')?([^;\n]*)/);
            if (match) downloadName = decodeURIComponent(match[1].replace(/"/g, ''));
        }

        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = downloadName;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);

        // alert('Arquivo baixado com sucesso!');

    } catch (err) {
        alert('Erro ao aplicar metadados: ' + err.message);
    } finally {
        showLoading(false);
    }
});

function showLoading(show) {
    loading.classList.toggle('hidden', !show);
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
