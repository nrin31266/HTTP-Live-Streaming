# HTTP-Live-Streaming-
/home/nrin31266/hls-data/videos/hls/
├── 12/                          # Movie ID = 12
│   ├── 360p/                    # Chất lượng 360p (640x360)
│   │   ├── segment_000.ts       # Các segment video (mỗi đoạn 6 giây)
│   │   ├── segment_001.ts
│   │   ├── ...
│   │   └── playlist.m3u8        # Playlist cho 360p
│   ├── 720p/                    # Chất lượng 720p (1280x720)
│   │   ├── segment_000.ts
│   │   ├── segment_001.ts
│   │   ├── ...
│   │   └── playlist.m3u8        # Playlist cho 720p
│   └── master.m3u8              # Master playlist (chọn chất lượng)
└── 3/                           # Movie ID = 3
    ├── 360p/
    ├── 720p/
    └── master.m3u8