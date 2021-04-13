import React from 'react';
import axios from 'axios';
import './App.css';
import { MediaModel } from './MediaModel';


class App extends React.Component {
  
  constructor(props) {
    super(props);
    this.handleSubmit = this.handleSubmit.bind(this);
    this.fileInput = React.createRef();
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
      let newMedias = this.state.medias;
      response.data._embedded.medias.map((data, i) => {
        newMedias.push(new MediaModel(data));
      });
      this.setState( {medias: newMedias });
      console.log(this.state.medias);
    });
  }

  handleClick() {
    //this.state.medias[0].update({ "fileExtension": "basu" });
    //this.state.medias[0].delete();
    let newMedias = this.state.medias;
    //newMedias[0] = newMedias[0].refresh();
    /*
    this.setState({
      medias: newMedias
    });
    */
    newMedias[0].refresh()
  }

  handleSubmit(event) {
    
    // Prevent the default submit
    event.preventDefault();

    // Define API constants
    const url = 'http://localhost:8080/media';
    const formData = new FormData();
    const config = {
        headers: {
          'content-type': 'multipart/form-data'
        }
      }
    
    // Loop through each file and append them to the form
    Object.keys(this.fileInput.current.files).forEach(key => {
      formData.append("files", this.fileInput.current.files[key]);
    });
    
    // Send the files to the backend
    axios.post(url, formData,config)
    .catch(function (error) {
      console.log(error);
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
          <form onSubmit={this.handleSubmit}>
            <input type="file" ref={this.fileInput} multiple/>
            <button type="submit">Submit</button>
          </form>
          <ul>
            {
                this.state.medias.map((media, index) => {
                  return <li key={ media.id }>{ media.fileName }</li>;
                })
            }
          </ul>
          <button onClick={() => this.handleClick()}>Update</button>
            {
                this.state.medias.map((media, index) => {
                  return <img src={ media.url} />
                })
            }
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
//TODO delete this line