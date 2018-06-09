(ns kundel.css)

(defn get-styles [font-min font-max]
  (str "

  .narrator-frame {
    height: 100%;
    width: 100%;
    display: flex;
    flex-direction: column;
  }

  .narrator-sections {
    height: calc(100% - 32px);
    width: 100%;
    display: flex;
    flex-direction: column;
    justify-content: space-evenly;
  }

  .narrator-buttons {
    height: 32px;
    width: 100%;
    display: flex;
    flex-direction: row;
    justify-content: center;
  }

  .narrator-button {
    height: 32px;
    margin-left: 5px;
    margin-right: 5px;
    cursor: pointer;
  }

  .narrator-sections-center {
    position: fixed;
    top: 50%;
    left: 50%;
    width: 0px;
    height: 0px;
  }

  .narrator-sections-center-play {
    position: absolute;
    top: -128px;
    left: -128px;
    width: 256px;
    height: 256px;
    background-size: 256px 256px;
    background: url('data:image/svg+xml;utf8;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iaXNvLTg4NTktMSI/Pgo8IS0tIEdlbmVyYXRvcjogQWRvYmUgSWxsdXN0cmF0b3IgMTkuMC4wLCBTVkcgRXhwb3J0IFBsdWctSW4gLiBTVkcgVmVyc2lvbjogNi4wMCBCdWlsZCAwKSAgLS0+CjxzdmcgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgdmVyc2lvbj0iMS4xIiBpZD0iQ2FwYV8xIiB4PSIwcHgiIHk9IjBweCIgdmlld0JveD0iMCAwIDQxLjk5OSA0MS45OTkiIHN0eWxlPSJlbmFibGUtYmFja2dyb3VuZDpuZXcgMCAwIDQxLjk5OSA0MS45OTk7IiB4bWw6c3BhY2U9InByZXNlcnZlIiB3aWR0aD0iMjU2cHgiIGhlaWdodD0iMjU2cHgiPgo8cGF0aCBkPSJNMzYuMDY4LDIwLjE3NmwtMjktMjBDNi43NjEtMC4wMzUsNi4zNjMtMC4wNTcsNi4wMzUsMC4xMTRDNS43MDYsMC4yODcsNS41LDAuNjI3LDUuNSwwLjk5OXY0MCAgYzAsMC4zNzIsMC4yMDYsMC43MTMsMC41MzUsMC44ODZjMC4xNDYsMC4wNzYsMC4zMDYsMC4xMTQsMC40NjUsMC4xMTRjMC4xOTksMCwwLjM5Ny0wLjA2LDAuNTY4LTAuMTc3bDI5LTIwICBjMC4yNzEtMC4xODcsMC40MzItMC40OTQsMC40MzItMC44MjNTMzYuMzM4LDIwLjM2MywzNi4wNjgsMjAuMTc2eiIgZmlsbD0iIzAwMDAwMCIvPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8L3N2Zz4K');

    opacity: 0;
    animation: toggle .5s linear;
  }

  .narrator-sections-center-pause {
    position: absolute;
    top: -128px;
    left: -128px;
    width: 256px;
    height: 256px;
    background-size: 256px 256px;
    background: url('data:image/svg+xml;utf8;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iaXNvLTg4NTktMSI/Pgo8IS0tIEdlbmVyYXRvcjogQWRvYmUgSWxsdXN0cmF0b3IgMTYuMC4wLCBTVkcgRXhwb3J0IFBsdWctSW4gLiBTVkcgVmVyc2lvbjogNi4wMCBCdWlsZCAwKSAgLS0+CjwhRE9DVFlQRSBzdmcgUFVCTElDICItLy9XM0MvL0RURCBTVkcgMS4xLy9FTiIgImh0dHA6Ly93d3cudzMub3JnL0dyYXBoaWNzL1NWRy8xLjEvRFREL3N2ZzExLmR0ZCI+CjxzdmcgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgdmVyc2lvbj0iMS4xIiBpZD0iQ2FwYV8xIiB4PSIwcHgiIHk9IjBweCIgd2lkdGg9IjI1NnB4IiBoZWlnaHQ9IjI1NnB4IiB2aWV3Qm94PSIwIDAgMzU3IDM1NyIgc3R5bGU9ImVuYWJsZS1iYWNrZ3JvdW5kOm5ldyAwIDAgMzU3IDM1NzsiIHhtbDpzcGFjZT0icHJlc2VydmUiPgo8Zz4KCTxnIGlkPSJwYXVzZSI+CgkJPHBhdGggZD0iTTI1LjUsMzU3aDEwMlYwaC0xMDJWMzU3eiBNMjI5LjUsMHYzNTdoMTAyVjBIMjI5LjV6IiBmaWxsPSIjMDAwMDAwIi8+Cgk8L2c+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPC9zdmc+Cg==');
    opacity: 0;
    animation: toggle .5s linear;
  }

  @keyframes toggle {
    0%   {
      opacity: 0;
    }
    50%   {
      opacity: .1;
    }
    100% {
      opacity: 0;
    }
  }

  .narrator-section {
    -webkit-transition: font-size .5s ease-in-out;
    -moz-transition: font-size .5s ease-in-out;
    -o-transition: font-size .5s ease-in-out;
    transition: font-size .5s ease-in-out;
    width: 95%;
  }

  .narrator-section.narrator-current {
    font-size: " font-max ";
  }

  .narrator-section:not(.narrator-current) {
    font-size: " font-min ";
  }

  .narrator-subsection-frame-expand {
    max-height: 0px;
    -webkit-transition: all .5s linear;
    -moz-transition: all .5s linear;
    -o-transition: all .5s linear;
    transition: all .5s linear;

    display: flex;
    width: 2em;
    margin-left: 40%;
    margin-top: 1em;
    margin-bottom: 1em;
    cursor: pointer;
  }

  .narrator-subsection-frame-expand img {
    margin-right: .25em;
  }

  .narrator-sections:not(.narrating) .narrator-section.narrator-current:not(.has-narrator-subsections) .narrator-subsection-frame-expand {
    max-height: 0px;
    margin-top: 0px;
    margin-bottom: 0px;
  }

  .narrator-sections:not(.narrating) .narrator-section.narrator-current.narrating-in-subsection .narrator-subsection-frame-expand {
    max-height: 0px;
    margin-top: 0px;
    margin-bottom: 0px;
  }

  .narrator-sections:not(.narrating) .narrator-section.narrator-current.has-narrator-subsections:not(.narrating-in-subsection) .narrator-subsection-frame-expand {
    max-height: 2em;
    margin-top: 1em;
    margin-bottom: 1em;
  }

  .narrator-sections.narrating .narrator-subsection-frame-expand {
    max-height: 0px;
    margin-top: 0px;
    margin-bottom: 0px;
  }

  .narrator-sections:not(.narrating) .narrator-section:not(.narrator-current) .narrator-subsection-frame-expand {
    max-height: 0px;
    margin-top: 0px;
    margin-bottom: 0px;
  }

  .narrator-subsection-frame {
    overflow: hidden;
    max-height: 0px;
    -webkit-transition: max-height .5s linear;
    -moz-transition: max-height .5s linear;
    -o-transition: max-height .5s linear;
    transition: max-height .5s linear;

    margin-right: 1em;
    margin-left: 1em;
    background-color: #DCDCDC;
    padding-right: 1em;
    padding-left: 1em;
    margin-top: .5em;
    margin-bottom: .5em;
  }

  .narrator-subsection-frame:not(.narrator-current) {
    max-height: 0px;
  }

  .narrator-subsection-frame.narrator-current {
    max-height: 500px;
  }

  .narrator-susbection-carousel {
    display: flex;
    flex-wrap: nowrap;
    align-items: center;

    -webkit-transition: transform .25s linear;
    -moz-transition: transform .25s linear;
    -o-transition: transform .25s linear;
    transition: transform .25s linear;
  }

  .narrator-subsection {
    position: relative;
    display: inline-block;
    width: 100%;

    -webkit-transition: opacity .25s linear;
    -moz-transition: opacity .25s linear;
    -o-transition: opacity .25s linear;
    transition: opacity .25s linear;
  }

  .narrator-subsection:not(.narrator-current) {
    opacity: 0;
  }

  .narrator-subsection.narrator-current {
    opacity: 1
  }

  .narrator-flow {
    cursor: pointer;
  }

  .narrator-flow:not(.narrator-current) {
    color: #778899;
  }

  .narrator-flow.narrator-current {
    color: #000000;
  }

  "))
