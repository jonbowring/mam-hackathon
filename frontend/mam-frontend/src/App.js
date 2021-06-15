import React from 'react';
import axios from 'axios';
import './App.css';
import { MediaModel } from './MediaModel';
import 'react-notifications/lib/notifications.css';
import { NotificationContainer } from 'react-notifications';
import { NotificationManager } from 'react-notifications';
import Header  from './Header';
import Sidebar from './Sidebar';
import Button from '@material-ui/core/Button';
import PhotoLibraryIcon from '@material-ui/icons/PhotoLibrary';


function Derivatives(props) {
  
  const media = props.media;

  if(media !== null) {
    if(media.derivatives !== null) {
      const nodes = Object.entries(media.derivatives).map((pair, index) => {
        return <div key={pair[0]}><label className="popLabel">{ pair[0] }</label><Button className="downloadMedia" variant="contained" href={ pair[1] } download>Download</Button></div>
      });
      return nodes;
    }
    else {
      return <span/>
    }
  }
  else {
    return <span/>
  }
}

function FileList(props) {
  
  const files = props.files;

  if(files !== null) {
    if(files !== null) {
      const nodes = Object.entries(files).map((pair, index) => {
        
        return <span key={ pair[1].name } className="upload-file-name">{ pair[1].name }</span>
      });
      return nodes;
    }
    else {
      return <span/>
    }
  }
  else {
    return <span/>
  }
}

class App extends React.Component {
  
  constructor(props) {
    super(props);
    this.handleSubmit = this.handleSubmit.bind(this);
    this.fileInput = React.createRef();
    this.handleMedia = this.handleMedia.bind(this);
    this.fetchMedia = this.fetchMedia.bind(this);
    this.searchMedia = this.searchMedia.bind(this);
    this.DisplayEntireMedia = this.DisplayEntireMedia.bind(this);
    this.handleFilenameChange = this.handleFilenameChange.bind(this);
    this.state = {
      medias: [],
      selectedMedia: null,
      popoutVisible: false,
      popoutClass: 'app-aside-hidden',
      showVisible:true,
      showClass:'show',
      setVisible: false,
      setClass: 'hide',
      MediaClass:'app-content-hide',
      MediaVisible:false,
      hierarchyCode:'1234-1',
      allmedias:[],
      fileList: {}
     }
   
  }
  componentDidMount() {
    axios({
      method: 'get',
      url: 'http://mam-hackathon.com:8080/media'
    })
    .then((response) => {
      let newMedias = this.state.medias;
      response.data._embedded.medias.forEach((data, i) => {
        newMedias.push(new MediaModel(data));
      });
      this.setState( {medias: newMedias });
      this.setState( {allmedias: newMedias });
    });
  }
 
  DisplayEntireMedia() {
    
    axios({
      method: 'get',
      url: 'http://mam-hackathon.com:8080/media'
    })
    .then((response) => {
      this.setState(state=>({
        medias:[]}));
      let newMedias = this.state.medias;
      response.data._embedded.medias.forEach((data, i) => {
        newMedias.push(new MediaModel(data));
      });
      this.setState( {medias: newMedias });
    });
  }
 
  
  closePopout() {
    this.setState({ 
      'popoutClass': 'app-aside-hidden', 
      'popoutVisible': false,
      'showClass':'show',
      'showVisible':true,
    });
  }

  selectMedia(index, event) {
    
    let media = this.state.medias[index];
    let popoutClass = '';
    let popoutVisible = false;
    let sideClass = '';
    let sideVisible = false;
    let showClass = '';
    let showVisible = true;

    if(!this.state.popoutVisible) {
      
      popoutClass = 'app-aside-visible';
      popoutVisible = true;
      sideClass = 'side-visible';
      sideVisible = true;
      showClass = 'hide';
      showVisible = false;
      this.setState({ 
        'popoutClass': popoutClass, 
        'popoutVisible': popoutVisible,
        'selectedMedia': media ,
        'showClass':showClass,
        'showVisible':showVisible,
        'sideClass':sideClass,
        'sideVisible':sideVisible,
        'MediaVisible': false,
        'MediaClass': 'app-content-hide'
      });

    }
    else {

      this.setState({ 
        'selectedMedia': media 
      });

    }

  }

 


  deleteRow(id, e){  
    axios.delete(`http://mam-hackathon.com:8080/media/${id}`)  
      .then(res => {  
        console.log(res);  
        console.log(res.data);  
        NotificationManager.success('Media Deleted!','',500);
    
        const medias = this.state.medias.filter(item => item.id !== id);  
        
        this.setState({ medias });  
      })  
    
  }
  mediaLibrary(e){
    axios({
      method: 'get',
      url: `http://mam-hackathon.com:8080/mediaHierarchy`
      })
      .then((response) => {
      let newHierarchy = this.state.hierarchies;
        response.data._embedded.mediaHierarchies.forEach((data, i) => {
          console.log('hierarchy name -'+data.hierarchyName);
          this.setState({
          hierarchyName:this.state.hierarchyName.concat(data.hierarchyName)
        });
        
        this.setState({
          data:response.data._embedded.mediaHierarchies
        });
          
          
        //  return newHierarchy.push(new HierarchyModel(data));
        });
        this.setState( {hierarchies: newHierarchy });
        
      });
  }
  
  handleFilenameChange(event) {
    console.log(event.target.files);
    this.setState( { fileList: event.target.files } );
  }
  
  handleAttrChange(id, event) {
    
    axios({
			method: 'put',
			url: `http://mam-hackathon.com:8080/media/${id}`,
			data: {
        [event.target.id]: event.target.value
      }
		})
		.then((response) => {
			console.log(response)
		});
    
  }

  handleSubmit(event) {
    
    // Prevent the default submit
    event.preventDefault();

    // Define API constants
    const url = 'http://mam-hackathon.com:8080/media';
    const formData = new FormData();
    const config = {
        headers: {
          'content-type': 'multipart/form-data'
        }
      }
    
    // Loop through each file and append them to the form
    console.log(this.fileInput.current.files);
    Object.keys(this.fileInput.current.files).forEach(key => {
      formData.append("files", this.fileInput.current.files[key]);
    });
    
    // Send the files to the backend
    axios.post(url, formData,config).then(response=>{console.log(response.data);
      window.location.reload();})
    .catch(function (error) {
      console.log(error);
      NotificationManager.success( 'File Upload Failed!');
    });
    NotificationManager.success( 'File Upload was Successful!');

  }
  handleMedia(e){
    
    if(this.state.MediaVisible)
    {
    this.setState(state=>({
        MediaVisible:false,
        MediaClass:'app-content-hide'
    }
    ));
      }else if(!this.state.MediaVisible)
        {
            this.setState(state=>({
              MediaVisible:true,
              MediaClass:'app-content-menu',
              popoutClass: 'app-aside-hidden', 
              popoutVisible: false,
              showClass:'show',
              showVisible:true,
        }));
        }
  }
  fetchMedia(hierarchyCode,e) {
    axios({
      method: 'get',
      url: 'http://mam-hackathon.com:8080/media'
    })
    .then((response) => {
      this.setState(state=>({
        medias:[]}));
      let newMedias = this.state.medias;
      response.data._embedded.medias.forEach((data, i) => {
        if(data.hierarchyCode===hierarchyCode) {
          newMedias.push(new MediaModel(data));
        }
        
      });
      this.setState( {medias: newMedias });
    });
  }
  searchMedia(e) {
    let value = e.target.value.toLowerCase();
        let result = [];
        console.log(this.state.medias);
        
        result = this.state.allmedias.filter((data) => {
            
        return data.fileName.toLowerCase().search(value) !== -1 || data.id.search(value) !== -1;
        });
        this.setState(state=>({
          medias:[]}));
          this.setState( {medias: result });
          console.log(result);
  }

  render() {
    //const hierarchyCode=this.state.hierarchyCode;
    return (
     
     <div className="App">
        <Header/>
        
        <div className='app__page'> 
        <Sidebar className="this.state.side-class" action={ this.handleMedia }  mediaAction={ this.fetchMedia }  mediaSearch={ this.searchMedia } displayMedia={ this.DisplayEntireMedia } />
        <div className={ this.state.showClass }>
        
        <section className="app-content">
          <div className={ this.state.MediaClass }>
            <center>
              <h1>Media</h1>
              <form onSubmit={this.handleSubmit}>
                <div className="upload-file-list">
                  <FileList files={ this.state.fileList }/>
                </div>
                <Button onClick={() => this.fileInput.current && this.fileInput.current.click()} >
                  <PhotoLibraryIcon/>
                </Button>
                <input type="file" ref={this.fileInput} multiple style={{ display: 'none'}}  onChange={(event) => this.handleFilenameChange(event)} />
                
                <Button variant="contained" type="submit">Submit</Button>
              </form>
            </center>
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
        </div>
        </div>
       
        <aside className={ this.state.popoutClass }>
          <span className="closeButton" onClick={ () => this.closePopout() }>&times;</span>
          <form className="popForm">
          <div>
              
              <img className="popImage" src={ this.state.selectedMedia !== null ? this.state.selectedMedia.url : '' } alt={ this.state.selectedMedia !== null ? this.state.selectedMedia.fileName : '' }/>
          
            </div>
            
            <h3>Metadata:</h3>
            <div>
              <label className="popLabel">ID:</label>
              <label className="popReadOnly">{ this.state.selectedMedia !== null ? this.state.selectedMedia.id : '' }</label>
            </div>
            <div>
              <label className="popLabel">File Name:</label>
              <label className="popReadOnly">{ this.state.selectedMedia !== null ? this.state.selectedMedia.fileName : '' }</label>
            </div>
            <div>
              <label className="popLabel">File Extension:</label>
              <label className="popReadOnly">{ this.state.selectedMedia !== null ? this.state.selectedMedia.fileExtension : '' }</label>
            </div>
            <div>
              <label className="popLabel">File Encoding:</label>
              <input className="popInput" type="text" id="fileEncoding" name="popFileEncoding" defaultValue={ this.state.selectedMedia !== null ? this.state.selectedMedia.fileEncoding : '' } onChange={ (event) => this.handleAttrChange(this.state.selectedMedia.id, event) }/>
            </div>
            <div>
              <label className="popLabel">File Size:</label>
              <input className="popInput" type="text" id="fileSize" name="popFileSize" defaultValue={ this.state.selectedMedia !== null ? this.state.selectedMedia.fileSize : '' } onChange={ (event) => this.handleAttrChange(this.state.selectedMedia.id, event) }/>
            </div>
            <div>
              <label className="popLabel">Mime Type:</label>
              <input className="popInput" type="text" id="mimeType" name="popMimeType" defaultValue={ this.state.selectedMedia !== null ? this.state.selectedMedia.mimeType : '' } onChange={ (event) => this.handleAttrChange(this.state.selectedMedia.id, event) }/>
            </div>
            <div>
              <label className="popLabel">URL:</label>
              <label className="popReadOnly">{ this.state.selectedMedia !== null ? this.state.selectedMedia.url : '' }</label>
            </div>
            <div>
              <label htmlFor="hierarchy" className="popLabel">Hierarchy Code:</label>
              <input className="popInput" type="text" id="hierarchyCode" name="popHierarchyCode" list="hierarchyList" defaultValue={ this.state.selectedMedia !== null ? this.state.selectedMedia.hierarchyCode : '' } onChange={ (event) => this.handleAttrChange(this.state.selectedMedia.id, event) }/>
             
              <datalist id="hierarchyList">
                       <option value="1234-1">Product Images</option>
                       <option value="1234-2">Item Images</option>
                       <option value="1235-1">SDS</option>
                       <option value="1235-2">Reports</option>
              </datalist>
               
            </div>
            
            <h3>Download media asset:</h3>
            <div>
              <label className="popLabel">original</label>
              <Button className="downloadMedia" variant="contained" href={ this.state.selectedMedia !== null ? this.state.selectedMedia.url : '' } download>Download</Button>
            </div>
            <Derivatives media={this.state.selectedMedia}/>

            <h3>Delete media asset:</h3>
            <div id='div1'>
              <Button className="deleteMedia" variant="contained"  onClick={() => this.deleteRow(this.state.selectedMedia.id)}>Delete</Button>
            </div>
          </form>
          
          
        </aside>
        
        <NotificationContainer />
      </div>
      
    ); // End return
  } // End render()
  
} // End class App
export default App;
//TODO delete this line