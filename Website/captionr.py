from flask import Flask, render_template, request, redirect, url_for, send_from_directory
from flask_bootstrap import Bootstrap
from threading import Thread
from subprocess import call
import pafy
import os

BASE_DIRECTORY_PATH = '../data'
SUBTITLE_PATH = BASE_DIRECTORY_PATH + '/subtitle/'
TRANSCIRPT_PATH = BASE_DIRECTORY_PATH + '/transcript/'
AUDIO_STAGING_PATH = BASE_DIRECTORY_PATH + '/audio_staging/'
AUDIO_PATH = BASE_DIRECTORY_PATH + '/audio'


app = Flask(__name__)
Bootstrap(app)

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/create/', methods=["GET", "POST"])
@app.route('/create/<blah>', methods=["GET"])
def create(blah=False):
    if request.method == "POST":
        transcript = request.form['transcript']
        youtube_id = request.form['videoID']
        subtitle = open('../data/subtitle/' + youtube_id + '.vtt', 'w+')
        subtitle.write('')

        transcript_file = open(TRANSCIRPT_PATH + youtube_id + '.txt', 'w+')
        transcript_file.write(transcript)
        Thread(target=handle_youtube, args=(youtube_id,)).start()
        return redirect(url_for('play', video_id=youtube_id));
    else:
        return render_template('create.html')

@app.route('/play/<video_id>/')
def play(video_id):
    sub_file_name = SUBTITLE_PATH + video_id + '.vtt'
    if not os.path.isfile(sub_file_name):
        return render_template('nosubs.html', vid=video_id)
    elif os.path.getsize(sub_file_name) == 0:
        return render_template('processing.html')
    else:
        return render_template('play.html', subfile=url_for('subtitle', path=(video_id + '.vtt')), vid=video_id)
    


@app.route('/subtitle/<path:path>')
def subtitle(path):
    return send_from_directory(SUBTITLE_PATH, path)

@app.route('/browse/')
def browse():
    videos_subed = os.listdir(SUBTITLE_PATH)
    video_list = []
    for x in videos_subed:
        video = x[:x.rindex('.')]
        thumbnail = pafy.new(video).thumb
        video_list.append((video, thumbnail))
    return render_template('browse.html', vid_list=video_list)



def handle_youtube(video_id):
    path = download_youtube(video_id)
    process_audio(path)
    os.remove(path)

def download_youtube(video_id):
    video = pafy.new(video_id)
    best_audio = video.getbestaudio()
    download_path = AUDIO_STAGING_PATH + video_id + '.' + best_audio.extension
    best_audio.download(quiet=True, filepath=download_path)
    return download_path
def process_audio(path):
    wav_file = path[path.rindex('/'):path.rindex('.')] + ".wav"
    call(["ffmpeg", "-i", path, "-sample_rate", "16000", AUDIO_STAGING_PATH + wav_file, "-y"])
    os.remove(path)
    os.rename(AUDIO_STAGING_PATH + wav_file, AUDIO_PATH + wav_file)

if __name__ == '__main__':
    app.run(debug=True)


