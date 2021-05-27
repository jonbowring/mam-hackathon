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



class App extends React.Component {
  
  constructor(props) {
    super(props);
    this.handleSubmit = this.handleSubmit.bind(this);
    this.fileInput = React.createRef();
    this.handleMedia = this.handleMedia.bind(this);
    this.fetchMedia = this.fetchMedia.bind(this);
    this.searchMedia = this.searchMedia.bind(this);
    this.DisplayEntireMedia = this.DisplayEntireMedia.bind(this);
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
      allmedias:[]
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
      this.setState( {allmedias: newMedias });
    });
  }
 
  DisplayEntireMedia() {
    axios({
      method: 'get',
      url: 'http://localhost:8080/media'
    })
    .then((response) => {
      this.setState(state=>({
        medias:[]}));
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
        NotificationManager.success('Media Deleted!','',500);
    
        const medias = this.state.medias.filter(item => item.id !== id);  
        
        this.setState({ medias });  
      })  
    
  }
  mediaLibrary(e){
    axios({
      method: 'get',
      url: `http://localhost:8080/mediaHierarchy`
      })
      .then((response) => {
      let newHierarchy = this.state.hierarchies;
        response.data._embedded.mediaHierarchies.map((data, i) => {
          console.log('hierarchy name -'+data.hierarchyName);
          this.setState({
          hierarchyName:this.state.hierarchyName.concat(data.hierarchyName)
        })
        
        this.setState({
          data:response.data._embedded.mediaHierarchies
        })
          
          
        //  return newHierarchy.push(new HierarchyModel(data));
        });
        this.setState( {hierarchies: newHierarchy });
        
      });
  }
  handleAttrChange(id, event) {
    
    axios({
			method: 'put',
			url: `http://localhost:8080/media/${id}`,
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
             MediaClass:'app-content-menu'
        }));
        }
  }
  fetchMedia(hierarchyCode,e) {
    axios({
      method: 'get',
      url: 'http://localhost:8080/media'
    })
    .then((response) => {
      this.setState(state=>({
        medias:[]}));
      let newMedias = this.state.medias;
      response.data._embedded.medias.map((data, i) => {
        if(data.hierarchyCode===hierarchyCode)
        return newMedias.push(new MediaModel(data));
      });
      this.setState( {medias: newMedias });
    });
  }
  searchMedia(e) {
    let value = e.target.value.toLowerCase();
        let result = [];
        console.log(this.state.medias);
        
        result = this.state.allmedias.filter((data) => {
            
        return data.fileName.toLowerCase().search(value) != -1 || data.id.search(value) != -1;
        });
        this.setState(state=>({
          medias:[]}));
          this.setState( {medias: result });
          console.log(result);
  }
  render() {
    const hierarchyCode=this.state.hierarchyCode;
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
            <br/><br/>
            <form onSubmit={this.handleSubmit}>
            <Button onClick={() => this.fileInput.click()}>
            <PhotoLibraryIcon/>
            </Button>
            <input type="file"ref={(fileInput) => {
                    this.fileInput = fileInput;
                  }} multiple style={{ display: 'none'}}  />
            
           
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
              
              <img className="popImage"  src={ this.state.selectedMedia != null ? this.state.selectedMedia.url : '' }/>
              <div id='div1'>
            <Button className="deleteMedia" variant="contained"  onClick={(e) => this.deleteRow(this.state.selectedMedia.id, e)}>Delete</Button>


          </div>
          <div id='div2'>
          <Button className="downloadMedia" variant="contained"   href={ this.state.selectedMedia != null ? this.state.selectedMedia.url : '' } download>Download</Button>
          </div>
            </div>
            <div>
              <label className="popLabel">ID:</label>
              <label className="popInput">{ this.state.selectedMedia != null ? this.state.selectedMedia.id : '' }</label>
            </div>
            <div>
              <label className="popLabel">File Name:</label>
              <label className="popInput">{ this.state.selectedMedia != null ? this.state.selectedMedia.fileName : '' }</label>
            </div>
            <div>
              <label className="popLabel">File Extension:</label>
              <label className="popInput">{ this.state.selectedMedia != null ? this.state.selectedMedia.fileExtension : '' }</label>
            </div>
            <div>
              <label className="popLabel">File Encoding:</label>
              <input className="popInput" type="text" id="fileEncoding" name="popFileEncoding" defaultValue={ this.state.selectedMedia != null ? this.state.selectedMedia.fileEncoding : '' } onChange={ (event) => this.handleAttrChange(this.state.selectedMedia.id, event) }/>
            </div>
            <div>
              <label className="popLabel">File Size:</label>
              <input className="popInput" type="text" id="fileSize" name="popFileSize" defaultValue={ this.state.selectedMedia != null ? this.state.selectedMedia.fileSize : '' } onChange={ (event) => this.handleAttrChange(this.state.selectedMedia.id, event) }/>
            </div>
            <div>
              <label className="popLabel">Mime Type:</label>
              <input className="popInput" type="text" id="mimeType" name="popMimeType" defaultValue={ this.state.selectedMedia != null ? this.state.selectedMedia.mimeType : '' } onChange={ (event) => this.handleAttrChange(this.state.selectedMedia.id, event) }/>
            </div>
            <div>
              <label className="popLabel">URL:</label>
              <label className="popInput">{ this.state.selectedMedia != null ? this.state.selectedMedia.url : '' }</label>
            </div>
            <div>
              <label for="hierarchy" className="popLabel">Hierarchy Code:</label>
              <input className="popInput" type="text" id="hierarchyCode" name="popHierarchyCode" list="hierarchyList" defaultValue={ this.state.selectedMedia != null ? this.state.selectedMedia.hierarchyCode : '' } onChange={ (event) => this.handleAttrChange(this.state.selectedMedia.id, event) }/>
             
              <datalist id="hierarchyList">
                       <option value="1234-1">Product Images</option>
                       <option value="1234-2">Item Images</option>
                       <option value="1235-1">SDS</option>
                       <option value="1235-2">Reports</option>
              </datalist>
               
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