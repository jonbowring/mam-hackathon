import React from 'react';
import ReactDOM from 'react-dom';
import axios from 'axios';
import './App.css';
import { MediaModel } from './MediaModel';


class App extends React.Component {
  
  constructor(props) {
    super(props);
    this.state = {
      medias: []
    }
  }

  componentDidMount() {
    axios({
      method: 'get',
      url: 'http://localhost:8080/media'
    })
    .then((response) => {
      response.data._embedded.medias.map((data, i) => {
        this.state.medias.push(new MediaModel(data));
      });
      console.log(this.state.medias);
    });
  }
  
  
  render() {
    
    return (
     
     <div className="App">
        <header className="app-header">
          <h1>Header</h1>
        </header>
        <nav className="app-nav">
          <h1>Nav</h1>
        </nav>
        <section className="app-content">
          <h1>Content</h1>
        </section>
        <footer className="app-footer">
          <h1>Footer</h1>
        </footer>
        <aside className="app-aside">
          <h1>Aside</h1>
        </aside>
      </div>

    ); // End return

  } // End render()
  
} // End class App

export default App;
