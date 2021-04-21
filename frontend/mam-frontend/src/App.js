import React from 'react';
import axios from 'axios';
import './App.css';
import InfaLogo from './infa-logo.svg';
import { MediaModel } from './MediaModel';

class App extends React.Component {
  
  constructor(props) {
    super(props);
    this.handleSubmit = this.handleSubmit.bind(this);
    this.fileInput = React.createRef();
    this.state = {
      medias: [],
      selectedMedia: null,
      popoutVisible: false,
      popoutClass: 'app-aside-hidden'
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
        return newMedias.push(new MediaModel(data));
      });
      this.setState( {medias: newMedias });
    });
  }
  
  closePopout() {
    this.setState({ 
      'popoutClass': 'app-aside-hidden', 
      'popoutVisible': false 
    });
  }

  selectMedia(index, event) {
    
    let media = this.state.medias[index];

    let popoutClass = '';
    let popoutVisible = false;
    
    if(!this.state.popoutVisible) {
      
      popoutClass = 'app-aside-visible';
      popoutVisible = true;

      this.setState({ 
        'popoutClass': popoutClass, 
        'popoutVisible': popoutVisible,
        'selectedMedia': media 
      });

    }
    else {

      this.setState({ 
        'selectedMedia': media 
      });

    }

  }
  

  deleteRow(id, e){  
    axios.delete(`http://localhost:8080/media/${id}`)  
      .then(res => {  
        console.log(res);  
        console.log(res.data);  
    
        const medias = this.state.medias.filter(item => item.id !== id);  
        this.setState({ medias });  
      })  
    
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
          <img className="infaLogo" src={ InfaLogo } alt="Informatica"/>
        </header>
        <nav className="app-nav">
          <h1>Nav</h1>
        </nav>
        <section className="app-content">
          <div className="app-content-menu">
            <h1>Media</h1>
            <form onSubmit={this.handleSubmit}>
              <input type="file" ref={this.fileInput} multiple/>
              <button type="submit">Submit</button>
            </form>
          </div>
          <div className="app-content-outer">
            <div className="app-content-inner">
              {
                  this.state.medias.map((media, index) => {
                    return <img key={ media.id } src={ media.url} alt={ media.fileName } className="grid-image" onClick={ (event) => this.selectMedia(index, event) } />
                  })
              }
            </div>
          </div>
          
        </section>
        <footer className="app-footer">
          <h1>Footer</h1>
        </footer>
        <aside className={ this.state.popoutClass }>
          <span className="closeButton" onClick={ () => this.closePopout() }>&times;</span>
          <form className="popForm">
            <div>
              <label className="popLabel">ID:</label>
              <input className="popInput" type="text" id="popId" name="popId" defaultValue={ this.state.selectedMedia != null ? this.state.selectedMedia.id : '' }/>
            </div>
            <div>
              <label className="popLabel">File Name:</label>
              <input className="popInput" type="text" id="popFilename" name="popFilename" defaultValue={ this.state.selectedMedia != null ? this.state.selectedMedia.fileName : '' }/>
            </div>
            <div>
              <label className="popLabel">File Extension:</label>
              <input className="popInput" type="text" id="popFileExtension" name="popFileExtension" defaultValue={ this.state.selectedMedia != null ? this.state.selectedMedia.fileExtension : '' }/>
            </div>
            <div>
              <label className="popLabel">File Encoding:</label>
              <input className="popInput" type="text" id="popFileEncoding" name="popFileEncoding" defaultValue={ this.state.selectedMedia != null ? this.state.selectedMedia.fileEncoding : '' }/>
            </div>
            <div>
              <label className="popLabel">File Size:</label>
              <input className="popInput" type="text" id="popFileSize" name="popFileSize" defaultValue={ this.state.selectedMedia != null ? this.state.selectedMedia.fileSize : '' }/>
            </div>
            <div>
              <label className="popLabel">Mime Type:</label>
              <input className="popInput" type="text" id="popMimeType" name="popMimeType" defaultValue={ this.state.selectedMedia != null ? this.state.selectedMedia.mimeType : '' }/>
            </div>
            <div>
              <label className="popLabel">URL:</label>
              <input className="popInput" type="text" id="popURL" name="popURL" defaultValue={ this.state.selectedMedia != null ? this.state.selectedMedia.url : '' }/>
            </div>
          </form>
        </aside>
      </div>
    ); // End return
  } // End render()
  
} // End class App
export default App;
//TODO delete this line