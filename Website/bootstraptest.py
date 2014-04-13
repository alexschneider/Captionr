from flask import Flask, render_template, request, redirect, url_for
from flask_bootstrap import Bootstrap
from threading import Thread
from subprocess import call
import pafy
import os

BASE_DIRECTORY_PATH = '../data'

def create_app():
    app = Flask(__name__)
    Bootstrap(app)

    @app.route('/')
    def index():
        return render_template('index.html')
    
    @app.route('/browse/')
    def browse():
        return render_template('browse.html')

    @app.route('/create/', methods=["GET", "POST"])
    def create():
        if request.method == "POST":
            transcript = request.form['transcript']
            youtube_id = request.form['videoID']
            subtitle = open('../data/subtitle/' + youtube_id + '.vtt', 'w+')
            subtitle.write('')

            transcript_file = open('../data/transcript/' + youtube_id + '.txt', 'w+')
            transcript_file.write(transcript)
            Thread(target=handle_youtube, args=(youtube_id,)).start()
            return redirect(url_for('play', video_id=youtube_id));
        else:
            return render_template('create.html')

    @app.route('/play/<video_id>/')
    def play(video_id):
        sub_file_name = BASE_DIRECTORY_PATH + '/subtitle/' + video_id + '.vtt'
        if not os.path.isfile(sub_file_name):
            return render_template('nosubs.html', vid=video_id)
        elif os.path.getsize(sub_file_name) == 0:
            return render_template('processing.html')
        else:
            return render_template('play.html', subfile=sub_file_name)
    return app


def handle_youtube(video_id):
    path = download_youtube(video_id)
    process_audio(path)
    os.remove(path)

def download_youtube(video_id):
    video = pafy.new(video_id)
    best_audio = video.getbestaudio()
    download_path = BASE_DIRECTORY_PATH + '/audio_staging/' + video_id + '.' + best_audio.extension
    best_audio.download(quiet=True, filepath=download_path)
    return download_path
def process_audio(path):
    wav_path = path[:path.rindex('.')] + ".wav"
    print(path)
    print(wav_path)
    call(["ffmpeg", "-i", path, "-sample_rate", "16000", wav_path, "-y"])

if __name__ == '__main__':
    create_app().run(debug=True)


