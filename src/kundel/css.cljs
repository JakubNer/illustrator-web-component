(ns kundel.css)

(defn get-styles [font-min font-max id]
  (str "

  .illustrator-frame {
    height: 100%;
    width: 100%;
    display: flex;
    flex-direction: column;
  }

  .illustrator-sections {
    height: calc(100% - 32px);
    width: 100%;
    display: flex;
    flex-direction: column;
    justify-content: center;
  }

  .illustrator-buttons {
    height: 74px;
    width: 100%;
    display: flex;
    flex-direction: row;
    justify-content: center;
    align-items: flex-start;
    position: relative;
  }

  @keyframes illustration-progress {
    0%   {
      -webkit-transform: rotate(0deg); ;
      -moz-transform: rotate(0deg); ;
      -o-transform: rotate(0deg); ;
      transform: rotate(0deg); ;
    }
    100% {
      -webkit-transform: rotate(360deg); ;
      -moz-transform: rotate(360deg); ;
      -o-transform: rotate(360deg); ;
      transform: rotate(360deg); ;
    }
  }

  .illustrator-button {
    height: 64px;
    margin-left: 20px;
    margin-right: 20px;
    cursor: pointer;
    transition: max-height 0.25s ease-out, margin-left 0.25s ease-out, margin-right 0.25s ease-out;
    -webkit-transition: max-height 0.25s ease-out, margin-left 0.25s ease-out, margin-right 0.25s ease-out;
    -moz-transition: max-height 0.25s ease-out, margin-left 0.25s ease-out, margin-right 0.25s ease-out;
    -o-transition: max-height 0.25s ease-out, margin-left 0.25s ease-out, margin-right 0.25s ease-out;
  }

  .illustrator-sections-center {
    position: fixed;
    top: 50%;
    left: 50%;
    width: 0px;
    height: 0px;
  }

  #illustrator-sections-center-overlay" id " {
    pointer-events: none;
  }

  .illustrator-sections-center-play {
    position: absolute;
    top: -128px;
    left: -128px;
    width: 256px;
    height: 256px;
    background-size: 256px 256px;
    background: url('data:image/svg+xml;utf8;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iaXNvLTg4NTktMSI/Pgo8IS0tIEdlbmVyYXRvcjogQWRvYmUgSWxsdXN0cmF0b3IgMTkuMC4wLCBTVkcgRXhwb3J0IFBsdWctSW4gLiBTVkcgVmVyc2lvbjogNi4wMCBCdWlsZCAwKSAgLS0+CjxzdmcgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgdmVyc2lvbj0iMS4xIiBpZD0iQ2FwYV8xIiB4PSIwcHgiIHk9IjBweCIgdmlld0JveD0iMCAwIDQxLjk5OSA0MS45OTkiIHN0eWxlPSJlbmFibGUtYmFja2dyb3VuZDpuZXcgMCAwIDQxLjk5OSA0MS45OTk7IiB4bWw6c3BhY2U9InByZXNlcnZlIiB3aWR0aD0iMjU2cHgiIGhlaWdodD0iMjU2cHgiPgo8cGF0aCBkPSJNMzYuMDY4LDIwLjE3NmwtMjktMjBDNi43NjEtMC4wMzUsNi4zNjMtMC4wNTcsNi4wMzUsMC4xMTRDNS43MDYsMC4yODcsNS41LDAuNjI3LDUuNSwwLjk5OXY0MCAgYzAsMC4zNzIsMC4yMDYsMC43MTMsMC41MzUsMC44ODZjMC4xNDYsMC4wNzYsMC4zMDYsMC4xMTQsMC40NjUsMC4xMTRjMC4xOTksMCwwLjM5Ny0wLjA2LDAuNTY4LTAuMTc3bDI5LTIwICBjMC4yNzEtMC4xODcsMC40MzItMC40OTQsMC40MzItMC44MjNTMzYuMzM4LDIwLjM2MywzNi4wNjgsMjAuMTc2eiIgZmlsbD0iIzAwMDAwMCIvPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8L3N2Zz4K');

    opacity: 0;
    animation: toggle .5s ease-out;
  }

  .illustrator-sections-center-pause {
    position: absolute;
    top: -128px;
    left: -128px;
    width: 256px;
    height: 256px;
    background-size: 256px 256px;
    background: url('data:image/svg+xml;utf8;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iaXNvLTg4NTktMSI/Pgo8IS0tIEdlbmVyYXRvcjogQWRvYmUgSWxsdXN0cmF0b3IgMTYuMC4wLCBTVkcgRXhwb3J0IFBsdWctSW4gLiBTVkcgVmVyc2lvbjogNi4wMCBCdWlsZCAwKSAgLS0+CjwhRE9DVFlQRSBzdmcgUFVCTElDICItLy9XM0MvL0RURCBTVkcgMS4xLy9FTiIgImh0dHA6Ly93d3cudzMub3JnL0dyYXBoaWNzL1NWRy8xLjEvRFREL3N2ZzExLmR0ZCI+CjxzdmcgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgdmVyc2lvbj0iMS4xIiBpZD0iQ2FwYV8xIiB4PSIwcHgiIHk9IjBweCIgd2lkdGg9IjI1NnB4IiBoZWlnaHQ9IjI1NnB4IiB2aWV3Qm94PSIwIDAgMzU3IDM1NyIgc3R5bGU9ImVuYWJsZS1iYWNrZ3JvdW5kOm5ldyAwIDAgMzU3IDM1NzsiIHhtbDpzcGFjZT0icHJlc2VydmUiPgo8Zz4KCTxnIGlkPSJwYXVzZSI+CgkJPHBhdGggZD0iTTI1LjUsMzU3aDEwMlYwaC0xMDJWMzU3eiBNMjI5LjUsMHYzNTdoMTAyVjBIMjI5LjV6IiBmaWxsPSIjMDAwMDAwIi8+Cgk8L2c+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPC9zdmc+Cg==');
    opacity: 0;
    animation: toggle .5s ease-out;
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

  .illustrator-section {
    -webkit-transition: font-size .25s ease-in-out, line-height .25s ease-in-out;
    -moz-transition: font-size .25s ease-in-out, line-height .25s ease-in-out;
    -o-transition: font-size .25s ease-in-out, line-height .25s ease-in-out;
    transition: font-size .25s ease-in-out, line-height .25s ease-in-out;
    width: 95%;
    font-size: " font-max ";
    margin-top: 1em;
  }

  .illustrator-section.illustrator-current {
    font-size: " font-max ";
    line-height: 1em;
  }

  .illustrator-section:not(.illustrator-current) {
    font-size: " font-min ";
    line-height: 1em;
  }

  .illustrator-section:not(.illustrator-current) .illustrator-subsection-frame .illustrator-subsection-frame-hider {
    pointer-events: none;
  }

  .illustrator-subsection-frame-hider {
    overflow: hidden;
  }

  .illustrator-subsection-frame {
    position: relative;
    max-height: 0px;
    -webkit-transition: max-height .25s ease-out;
    -moz-transition: max-height .25s ease-out;
    -o-transition: max-height .25s ease-out;
    transition: max-height .25s ease-out;

    margin-right: 1em;
    margin-left: 1em;
    background-color: #F5F5F5;
    padding-right: 1em;
    padding-left: 1em;
    margin-top: .5em;
    margin-bottom: .5em;
  }

  .illustrator-section:not(.illustrator-current) .illustrator-subsection-frame {
    max-height: 0px;
  }

  .illustrator-section.illustrator-current .illustrator-subsection-frame {
    max-height: 500px;
  }

  .illustrator-subsection-frame-left {
    position: absolute;
    height: 64px;
    left: -48px;
    top: calc(50% - 32px);
    opacity: 0;
    cursor: default;

    -webkit-transition: opacity .25s ease-out;
    -moz-transition: opacity .25s ease-out;
    -o-transition: opacity .25s ease-out;
    transition: opacity .25s ease-out;
  }

  .illustrator-subsection-frame-right {
    position: absolute;
    height: 64px;
    right: -48px;
    top: calc(50% - 32px);
    opacity: 0;
    cursor: default;

    -webkit-transition: opacity .25s ease-out;
    -moz-transition: opacity .25s ease-out;
    -o-transition: opacity .25s ease-out;
    transition: opacity .25s ease-out;
  }

  .illustrator-sections.narrating .illustrator-subsection-frame-left {
    cursor: default;
    opacity: 0;
  }

  .illustrator-section:not(.illustrator-current) .illustrator-subsection-frame-left {
    cursor: default;
    opacity: 0;
  }

  .illustrator-subsection-frame:not(.has-previous-subsection) .illustrator-subsection-frame-left {
    cursor: default;
    opacity: 0;
  }

  .illustrator-sections:not(.narrating) .illustrator-section.illustrator-current .illustrator-subsection-frame.has-previous-subsection .illustrator-subsection-frame-left {
    cursor: pointer;
    opacity: 1;
  }

  .illustrator-sections.narrating .illustrator-subsection-frame-right {
    cursor: default;
    opacity: 0;
  }

  .illustrator-section:not(.illustrator-current) .illustrator-subsection-frame-right {
    cursor: default;
    opacity: 0;
  }

  .illustrator-subsection-frame:not(.has-next-subsection) .illustrator-subsection-frame-right {
    cursor: default;
    opacity: 0;
  }

  .illustrator-sections:not(.narrating) .illustrator-section.illustrator-current .illustrator-subsection-frame.has-next-subsection .illustrator-subsection-frame-right {
    cursor: pointer;
    opacity: 1;
  }

  .illustrator-susbection-carousel {
    display: flex;
    flex-wrap: nowrap;
    align-items: center;
    max-height: 500px;

    -webkit-transition: transform .25s ease-out;
    -moz-transition: transform .25s ease-out;
    -o-transition: transform .25s ease-out;
    transition: transform .25s ease-out;
  }

  .illustrator-subsection {
    position: relative;
    display: inline-block;
    width: 100%;
    padding-top: .75em;
    padding-bottom: .75em;


    -webkit-transition: opacity .25s ease-out;
    -moz-transition: opacity .25s ease-out;
    -o-transition: opacity .25s ease-out;
    transition: opacity .25s ease-out;
  }

  .illustrator-section:not(.illustrator-current) .illustrator-subsection {
    opacity: 0;
  }

  .illustrator-section.illustrator-current .illustrator-subsection {
    opacity: 1
  }

  .illustrator-flow {
    cursor: pointer;
  }

  .illustrator-flow:not(.illustrator-current) {
    color: #778899;
  }

  .illustrator-flow.illustrator-current {
    color: #000000;
  }
  "))
